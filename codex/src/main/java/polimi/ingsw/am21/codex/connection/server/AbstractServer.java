package polimi.ingsw.am21.codex.connection.server;

import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
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

  /**
   * A latch that is used to wait for the server to be ready
   */
  protected final CountDownLatch serverReadyLatch;

  protected AbstractServer(Integer port, GameController controller) {
    this.port = port;
    this.controller = controller;
    this.serverReadyLatch = new CountDownLatch(1);
  }

  /**
   * Starts the server
   */
  public abstract void start()
    throws MalformedURLException, RemoteException, PortUnreachableException, UnknownHostException, AlreadyBoundException;

  /**
   * Gracefully stops the server
   */
  public abstract void stop();

  public CountDownLatch getServerReadyLatch() {
    return serverReadyLatch;
  }
}
