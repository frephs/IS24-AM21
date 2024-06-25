package polimi.ingsw.am21.codex;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import polimi.ingsw.am21.codex.client.ClientGameEventHandler;
import polimi.ingsw.am21.codex.client.localModel.LocalGameBoard;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.RMI.RMIClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.AbstractServer;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIServer;
import polimi.ingsw.am21.codex.connection.server.Server;
import polimi.ingsw.am21.codex.connection.server.TCP.TCPServer;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.View;

class MainTest {

  boolean isCI() {
    return Objects.equals(System.getenv("CI"), "true");
  }

  @BeforeEach
  void setUp() {
    new Main.Options(true);
    new Cli.Options(true);
  }

  @Test
  @DisabledIf("isCI")
  void rmiClient() throws InterruptedException {
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      AtomicReference<RMIClientConnectionHandler> client1 =
        new AtomicReference<>();
      AtomicReference<RMIClientConnectionHandler> client2 =
        new AtomicReference<>();
      AtomicReference<RMIServer> server = new AtomicReference<>();
      final CountDownLatch clientLatch = new CountDownLatch(2);

      AtomicInteger i = new AtomicInteger(0);
      executor.execute(() -> {
        server.set(
          new RMIServer(
            ConnectionType.RMI.getDefaultPort(),
            new GameController()
          )
        );
        try {
          server.get().start();
        } catch (
          MalformedURLException
          | RemoteException
          | UnknownHostException
          | AlreadyBoundException
          | PortUnreachableException e
        ) {
          i.addAndGet(1);
          server.set(
            new RMIServer(
              ConnectionType.RMI.getDefaultPort() + i.get(),
              new GameController()
            )
          );
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });

      executor.execute(() -> {
        View view = new DummyView("client1");
        client1.set(
          new RMIClientConnectionHandler(
            "localhost",
            ConnectionType.RMI.getDefaultPort() + i.get(),
            view,
            new ClientGameEventHandler(view, view.getLocalModel()),
            UUID.randomUUID()
          )
        );
        client1.get().connect();
        clientLatch.countDown();
      });
      executor.execute(() -> {
        View view = new DummyView("client2");
        client2.set(
          new RMIClientConnectionHandler(
            "localhost",
            ConnectionType.RMI.getDefaultPort() + i.get(),
            view,
            new ClientGameEventHandler(view, view.getLocalModel()),
            UUID.randomUUID()
          )
        );
        client2.get().connect();
        clientLatch.countDown();
      });

      try {
        clientLatch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      List<Runnable> actions = getActions(client1, client2, server);
      actions.forEach(action -> {
        action.run();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }

  @Test
  @DisabledIf("isCI")
  void tcpClient() throws InterruptedException {
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      AtomicReference<TCPClientConnectionHandler> client1 =
        new AtomicReference<>();
      AtomicReference<TCPClientConnectionHandler> client2 =
        new AtomicReference<>();
      AtomicReference<TCPServer> server = new AtomicReference<>();
      final CountDownLatch clientLatch = new CountDownLatch(2);
      AtomicInteger i = new AtomicInteger(0);
      executor.execute(() -> {
        server.set(
          new TCPServer(
            ConnectionType.TCP.getDefaultPort(),
            new GameController()
          )
        );
        try {
          server.get().start();
        } catch (PortUnreachableException e) {
          i.addAndGet(0);
          server.set(
            new TCPServer(
              ConnectionType.TCP.getDefaultPort() + i.get(),
              new GameController()
            )
          );
        }

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });

      executor.execute(() -> {
        View view = new DummyView("client1");
        client1.set(
          new TCPClientConnectionHandler(
            "localhost",
            ConnectionType.TCP.getDefaultPort() + i.get(),
            view,
            new ClientGameEventHandler(view, view.getLocalModel())
          )
        );
        client1.get().connect();
        clientLatch.countDown();
      });
      executor.execute(() -> {
        View view = new DummyView("client2");
        client2.set(
          new TCPClientConnectionHandler(
            "localhost",
            ConnectionType.TCP.getDefaultPort(),
            view,
            new ClientGameEventHandler(view, view.getLocalModel())
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
    AtomicReference<? extends ClientConnectionHandler> client1,
    AtomicReference<? extends ClientConnectionHandler> client2,
    AtomicReference<? extends AbstractServer> server
  ) {
    final AtomicReference<ClientConnectionHandler> playingClient =
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

    Method localModelGetter;
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
          client1.get()
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
          .orElseThrow()
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

    actions.add(() -> {
      LocalGameBoard localGameBoard = localModel
        .getLocalGameBoard()
        .orElse(null);
      assertTrue(
        (localGameBoard != null &&
          client1.get() == playingClient.get() &&
          localGameBoard
            .getPlayer()
            .getPlayedCardsByPosition()
            .containsKey(new Position(0, 1)) &&
          !localGameBoard
            .getNextPlayer()
            .getPlayedCardsByPosition()
            .containsKey(new Position(0, 1))) ||
        (localGameBoard != null &&
          client2.get() == playingClient.get() &&
          localGameBoard
            .getCurrentPlayer()
            .getPlayedCardsByPosition()
            .containsKey(new Position(0, 1)) &&
          !localGameBoard
            .getPlayer()
            .getPlayedCardsByPosition()
            .containsKey(new Position(0, 1)))
      );
    });

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
