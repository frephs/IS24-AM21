package polimi.ingsw.am21.codex.connection.server.RMI;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import polimi.ingsw.am21.codex.Main;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;

class RMIServerTest {

  @BeforeEach
  void setUp() {
    new Main.Options(true);
    new Cli.Options(true);
  }

  boolean isCI() {
    return Objects.equals(System.getenv("CI"), "true");
  }

  @Test
  @DisabledIf("isCI")
  public void basic() {
    final CountDownLatch operationsLatch = new CountDownLatch(6);

    RMIServer server = new RMIServer(4567, new GameController());
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      executor.execute(() -> {
        try {
          server.start();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

      server.getServerReadyLatch().await();

      Registry registry = LocateRegistry.getRegistry(4567);

      RMIServerConnectionHandler client =
        (RMIServerConnectionHandler) registry.lookup("IS24-AM21-CODEX");

      //generate new random UUID

      UUID connectionID = UUID.randomUUID();
      assertDoesNotThrow(() -> {
        client.connect(UUID.randomUUID(), new DummyRemoteGameEventLister());
        client.connect(connectionID, new DummyRemoteGameEventLister());
        operationsLatch.countDown();
        client.createGame(connectionID, "TestGame", 4);
        operationsLatch.countDown();
        client.joinLobby(connectionID, "TestGame");
        operationsLatch.countDown();
        client.lobbySetTokenColor(connectionID, TokenColor.RED);
        operationsLatch.countDown();
        client.lobbySetNickname(connectionID, "TestNickname");
        operationsLatch.countDown();
        client.lobbyChooseObjective(connectionID, true);
        operationsLatch.countDown();
      });

      operationsLatch.await();

      server.stop();
    } catch (Exception e) {
      throw new RuntimeException("RMI error. " + e);
    }
  }
}
