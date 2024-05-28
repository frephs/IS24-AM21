package polimi.ingsw.am21.codex;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.Server;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

class MainTest {

  @Test
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
    List<Runnable> actions = new ArrayList<>();
    actions.add(() -> client1.get().createAndConnectToGame("test", 2));
    actions.add(() -> client1.get().lobbySetNickname("Player 1"));
    actions.add(() -> client1.get().lobbySetToken(TokenColor.RED));
    actions.add(() -> client2.get().connectToGame("test"));
    actions.add(() -> client2.get().lobbySetNickname("Player 2"));
    actions.add(() -> client2.get().lobbySetToken(TokenColor.BLUE));
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
