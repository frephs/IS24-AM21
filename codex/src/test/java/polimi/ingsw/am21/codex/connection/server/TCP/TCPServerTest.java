package polimi.ingsw.am21.codex.connection.server.TCP;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.CreateGameMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.JoinLobbyMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.SetTokenColorMessage;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetAvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

class TCPServerTest {

  ObjectOutputStream outputStream;
  ObjectInputStream inputStream;

  @Test
  public void basic() {
    List<Message> receivedMessages = new java.util.ArrayList<>();
    final CountDownLatch responsesLatch = new CountDownLatch(3);
    Socket clientSocket;

    TCPServer server = new TCPServer(4567, new GameController());
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
          } catch (IOException ignored) {} catch (ClassNotFoundException e) {
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
      List.of(
        new CreateGameMessage("TestGame", 4),
        new GetAvailableGameLobbiesMessage(),
        new JoinLobbyMessage("TestGame"),
        new SetTokenColorMessage(TokenColor.RED, "TestGame")
        //        new SetNicknameMessage("TestNickname", "TestGame")
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
          .containsAll(
            List.of(
              MessageType.GAME_CREATED,
              MessageType.AVAILABLE_GAME_LOBBIES,
              MessageType.CONFIRM
            )
          )
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
