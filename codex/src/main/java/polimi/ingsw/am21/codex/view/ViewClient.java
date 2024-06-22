package polimi.ingsw.am21.codex.view;

import java.util.concurrent.CountDownLatch;
import polimi.ingsw.am21.codex.client.ClientGameEventHandler;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.RMI.RMIClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPClientConnectionHandler;

public abstract class ViewClient {

  private final CountDownLatch isInitializedLatch = new CountDownLatch(1);

  protected ClientConnectionHandler client;
  protected View view;
  protected ClientGameEventHandler gameEventHandler;

  public ViewClient(View view) {
    this.view = view;
    this.gameEventHandler = new ClientGameEventHandler(
      view,
      view.getLocalModel()
    );
  }

  public void start(ConnectionType connectionType, String address, int port) {
    if (connectionType == ConnectionType.TCP) {
      client = new TCPClientConnectionHandler(
        address,
        port,
        view,
        gameEventHandler
      );
    } else {
      client = new RMIClientConnectionHandler(
        address,
        port,
        view,
        gameEventHandler
      );
    }
    client.connect();
    isInitializedLatch.countDown();
  }

  protected CountDownLatch getIsInitializedLatch() {
    return isInitializedLatch;
  }
}
