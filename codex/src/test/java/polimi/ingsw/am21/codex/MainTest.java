package polimi.ingsw.am21.codex;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPClientConnectionHandler;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

class MainTest {

  @Test
  void tcpClient() throws InterruptedException {
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      executor.execute(() -> {
        try {
          Main.main(new String[] { "--server" });
        } catch (
          MalformedURLException | RemoteException | NotBoundException e
        ) {
          throw new RuntimeException(e);
        }
      });

      AtomicReference<TCPClientConnectionHandler> client1 =
        new AtomicReference<>();
      AtomicReference<TCPClientConnectionHandler> client2 =
        new AtomicReference<>();
      final CountDownLatch clientLatch = new CountDownLatch(2);
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

      List<Runnable> actions = new ArrayList<>();
      actions.add(() -> client1.get().createAndConnectToGame("test", 2));
      actions.add(() -> client1.get().lobbySetNickname("Player 1"));
      actions.add(() -> client1.get().lobbySetToken(TokenColor.RED));
      actions.add(() -> client2.get().connectToGame("test"));
      actions.add(() -> client2.get().lobbySetNickname("Player 2"));
      actions.add(() -> client2.get().lobbySetToken(TokenColor.BLUE));

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
}
