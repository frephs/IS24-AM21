package polimi.ingsw.am21.codex;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.Server;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

class MainTest {

  boolean isCI() {
    return Objects.equals(System.getenv("CI"), "true");
  }

  @Test
  @DisabledIf("isCI")
  void tcpClient() throws InterruptedException {
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      AtomicReference<TCPClientConnectionHandler> client1 =
        new AtomicReference<>();
      AtomicReference<TCPClientConnectionHandler> client2 =
        new AtomicReference<>();
      AtomicReference<Server> server = new AtomicReference<>();
      final CountDownLatch clientLatch = new CountDownLatch(2);

      executor.execute(() -> {
        server.set(
          new Server(
            ConnectionType.TCP.getDefaultPort(),
            ConnectionType.RMI.getDefaultPort()
          )
        );
        try {
          server.get().start();
        } catch (
          MalformedURLException | RemoteException | PortUnreachableException e
        ) {
          throw new RuntimeException(e);
        }
      });

      executor.execute(() -> {
        client1.set(
          new TCPClientConnectionHandler(
            "localhost",
            ConnectionType.TCP.getDefaultPort(),
            new LocalModelContainer(new DummyView("client1"))
          )
        );
        client1.get().connect();
        clientLatch.countDown();
      });
      executor.execute(() -> {
        client2.set(
          new TCPClientConnectionHandler(
            "localhost",
            ConnectionType.TCP.getDefaultPort(),
            new LocalModelContainer(new DummyView("client2"))
          )
        );
        client2.get().connect();
        clientLatch.countDown();
      });

      clientLatch.await();

      List<Runnable> actions = getActions(client1, client2, server);
      actions.forEach(action -> {
        action.run();
        try {
          Thread.sleep(300);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }

  @NotNull
  private static List<Runnable> getActions(
    AtomicReference<TCPClientConnectionHandler> client1,
    AtomicReference<TCPClientConnectionHandler> client2,
    AtomicReference<Server> server
  ) {
    final AtomicReference<TCPClientConnectionHandler> playingClient =
      new AtomicReference<>(), notPlayingClient = new AtomicReference<>();

    List<Runnable> actions = new ArrayList<>();
    actions.add(() -> client1.get().createAndConnectToGame("test", 2));
    actions.add(() -> client1.get().lobbySetNickname("Player 1"));

    actions.add(() -> client1.get().lobbySetToken(TokenColor.RED));
    actions.add(() -> client2.get().connectToGame("test"));

    actions.add(() -> client2.get().lobbySetNickname("Player 2"));
    actions.add(() -> client2.get().lobbySetToken(TokenColor.BLUE));

    actions.add(() -> client1.get().lobbyChooseObjectiveCard(true));
    actions.add(() -> client2.get().lobbyChooseObjectiveCard(false));

    //todo remove duplicate messages
    actions.add(() -> client1.get().lobbyJoinGame(CardSideType.BACK));
    actions.add(() -> client2.get().lobbyJoinGame(CardSideType.BACK));

    //TODO remove duplicate messages
    actions.add(
      () -> client1.get().sendChatMessage(new ChatMessage("Player 1", "Hello"))
    );

    //making a private getter available to see whose turn is it

    Method localModelGetter = null;
    try {
      localModelGetter = ClientConnectionHandler.class.getDeclaredMethod(
          "getLocalModel"
        );
      assertNotNull(localModelGetter);
      localModelGetter.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    LocalModelContainer localModel;
    try {
      localModel = ((LocalModelContainer) localModelGetter.invoke(
          (ClientConnectionHandler) client1.get()
        ));
      assertNotNull(localModel);
    } catch (IllegalAccessException | InvocationTargetException e) {
      DummyView view = new DummyView("getActions");
      view.displayException(e);
      throw new RuntimeException(e);
    }

    actions.add(() -> {
      if (
        localModel
          .getLocalGameBoard()
          .getCurrentPlayer()
          .getNickname()
          .equals("Player 1")
      ) {
        playingClient.set(client1.get());
        notPlayingClient.set(client2.get());
      } else {
        playingClient.set(client2.get());
        notPlayingClient.set(client1.get());
      }
      playingClient.get().placeCard(0, CardSideType.BACK, new Position(0, 1));
    });

    actions.add(
      () ->
        playingClient.get().placeCard(1, CardSideType.FRONT, new Position(0, 2))
    );

    actions.add(
      () ->
        notPlayingClient
          .get()
          .placeCard(2, CardSideType.FRONT, new Position(0, 1))
    );

    actions.add(
      () ->
        assertTrue(
          (client1.get() == playingClient.get() &&
            localModel
              .getLocalGameBoard()
              .getPlayer()
              .getPlayedCards()
              .containsKey(new Position(0, 1)) &&
            !localModel
              .getLocalGameBoard()
              .getNextPlayer()
              .getPlayedCards()
              .containsKey(new Position(0, 1))) ||
          (client2.get() == playingClient.get() &&
            localModel
              .getLocalGameBoard()
              .getCurrentPlayer()
              .getPlayedCards()
              .containsKey(new Position(0, 1)) &&
            !localModel
              .getLocalGameBoard()
              .getPlayer()
              .getPlayedCards()
              .containsKey(new Position(0, 1)))
        )
    );

    actions.add(
      () ->
        assertTrue(client1.get().isConnected() && client2.get().isConnected())
    );

    actions.add(() -> client1.get().disconnect());
    actions.add(() -> client2.get().disconnect());
    actions.add(() -> server.get().stop());
    return actions;
  }
}
