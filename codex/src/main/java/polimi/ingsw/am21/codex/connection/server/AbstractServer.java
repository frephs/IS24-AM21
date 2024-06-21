package polimi.ingsw.am21.codex.connection.server;

import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;
import polimi.ingsw.am21.codex.controller.GameController;

public abstract class AbstractServer {

  /**
   * The Game controller to use
   */
  protected GameController controller;

  /**
   * The port to run the TCP server on
   */
  protected Integer port;

  protected final CountDownLatch serverReadyLatch;

  protected AbstractServer(Integer port, GameController controller) {
    this.port = port;
    this.controller = controller;
    this.serverReadyLatch = new CountDownLatch(1);
  }

  public abstract void start()
    throws MalformedURLException, RemoteException, PortUnreachableException;

  public abstract void stop();

  public CountDownLatch getServerReadyLatch() {
    return serverReadyLatch;
  }
}
