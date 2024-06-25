package polimi.ingsw.am21.codex.connection.server.TCP;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import polimi.ingsw.am21.codex.Main;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.clientActions.ConnectMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.CreateGameMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.JoinLobbyMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.SetNicknameMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.SetTokenColorMessage;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetAvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;

class TCPServerTest {

  @BeforeEach
  void setUp() {
    new Main.Options(true);
    new Cli.Options(true);
  }

  boolean isCI() {
    return Objects.equals(System.getenv("CI"), "true");
  }

  ObjectOutputStream outputStream;
  ObjectInputStream inputStream;

  @Test
  @DisabledIf("isCI")
  public void basic() {
    List<Message> receivedMessages = new java.util.ArrayList<>();

    // Please note that this list is not evaluated in order
    List<MessageType> expectedMessages = List.of(
      MessageType.GAME_CREATED,
      MessageType.AVAILABLE_GAME_LOBBIES,
      MessageType.PLAYER_JOINED_LOBBY,
      MessageType.LOBBY_INFO,
      MessageType.PLAYER_SET_TOKEN_COLOR,
      MessageType.PLAYER_SET_NICKNAME
    );

    final CountDownLatch responsesLatch = new CountDownLatch(
      expectedMessages.size()
    );
    Socket clientSocket;

    TCPServer server = new TCPServer(
      ConnectionType.TCP.getDefaultPort(),
      new GameController()
    );
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      executor.execute(() -> {
        try {
          server.start();
        } catch (PortUnreachableException e) {
          throw new RuntimeException(e);
        }
      });

      server.getServerReadyLatch().await();

      clientSocket = new Socket((String) null, 4567);
      clientSocket.setKeepAlive(true);
      clientSocket.setTcpNoDelay(true);
      System.out.println("Connected to server");

      try {
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
      } catch (IOException e) {
        System.err.println("Connection closed.");
        throw new RuntimeException(e);
      }

      executor.execute(() -> {
        System.out.println("Listening for responses...");
        while (true) synchronized (inputStream) {
          try {
            if (clientSocket.isClosed()) {
              System.out.println("Connection closed.");
              break;
            }
            Message message = (Message) inputStream.readObject();
            System.out.println("Received message: " + message);
            receivedMessages.add(message);
            responsesLatch.countDown();
          } catch (IOException ignored) {
            throw new RuntimeException();
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        }
      });

      executor.execute(() -> {
        while (true) {
          if (clientSocket.isClosed()) {
            System.out.println("Connection closed client.");
            break;
          }
        }
      });

      UUID connectionID = UUID.randomUUID();
      List.of(
        new ConnectMessage(connectionID),
        new CreateGameMessage(connectionID, "TestGame", 4),
        new GetAvailableGameLobbiesMessage(connectionID),
        new JoinLobbyMessage(connectionID, "TestGame"),
        new SetTokenColorMessage(connectionID, TokenColor.RED, "TestGame"),
        new SetNicknameMessage(connectionID, "TestNickname", "TestGame")
      ).forEach(message -> {
        try {
          outputStream.writeObject(message);
          outputStream.flush();
          outputStream.reset();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
      responsesLatch.await();

      assertTrue(
        receivedMessages
          .stream()
          .map(Message::getType)
          .toList()
          .containsAll(expectedMessages)
      );

      server.stop();
      clientSocket.close();
    } catch (IOException e) {
      throw new RuntimeException("Socket closed. " + e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
