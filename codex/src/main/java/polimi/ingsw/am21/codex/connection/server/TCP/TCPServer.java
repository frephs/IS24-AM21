package polimi.ingsw.am21.codex.connection.server.TCP;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import polimi.ingsw.am21.codex.controller.GameController;

public class TCPServer {

  /**
   * The port to run the TCP server on
   */
  private final Integer port;
  /**
   * The Game controller to use
   */
  private final GameController controller;
  /**
   * A map with the socket ids as keys and the corresponding connection handlers as values
   */
  private final Map<UUID, TCPServerConnectionHandler> activeHandlers;

  /**
   * The listener for the controller associated to this server
   */
  private final TCPServerControllerListener controllerListener;

  private ServerSocket serverSocket;
  private final CountDownLatch serverReadyLatch;

  public TCPServer(Integer port, GameController controller) {
    this.port = port;
    this.controller = controller;
    this.activeHandlers = new HashMap<>();

    this.controllerListener = new TCPServerControllerListener(message -> {
      for (TCPServerConnectionHandler handler : activeHandlers.values()) {
        handler.send(message);
      }
    });
    this.serverReadyLatch = new CountDownLatch(1);
  }

  public void start() throws PortUnreachableException {
    controller.registerGlobalListener(controllerListener);

    // Using try-with-resources here will automatically shut down the executor if
    // no longer needed, as it implements the AutoCloseable interface.
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      System.out.println("Starting TCP server on port " + port);

      serverSocket = new ServerSocket(port);
      serverReadyLatch.countDown();
      System.out.println("TCP server ready on port " + port);
      while (true) {
        UUID socketId = UUID.randomUUID();
        try {
          Socket connectionSocket = serverSocket.accept();
          System.out.println(
            "Client connected from " + connectionSocket.getInetAddress()
          );

          // TODO: Alternative listener handling: multiple listeners for each client
          //          controller.registerListener(
          //            socketId,
          //            new TCPServerControllerListener(message -> {
          //              for (TCPServerConnectionHandler handler : activeHandlers.values()) {
          //                handler.send(message);
          //              }
          //            })
          //          );

          TCPServerConnectionHandler handler = new TCPServerConnectionHandler(
            connectionSocket,
            controller,
            socketId,
            activeHandlers
          );
          activeHandlers.put(socketId, handler);

          executor.execute(handler);
        } catch (IOException error) {
          // Socket has been closed
          break;
        }
      }
    } catch (IOException error) {
      System.err.println("Can't start server on port " + port);
      System.err.println(error.getMessage());

      // Propagate the error so that the RMI client can be closed as well
      throw new PortUnreachableException();
    } finally {
      controller.unregisterGlobalListener(controllerListener);
    }
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException ignored) {}
    controller.unregisterGlobalListener(controllerListener);
    System.out.println("TCP Server stopped.");
  }

  public CountDownLatch getServerReadyLatch() {
    return serverReadyLatch;
  }
}
