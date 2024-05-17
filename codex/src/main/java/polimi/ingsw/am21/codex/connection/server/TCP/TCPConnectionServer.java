package polimi.ingsw.am21.codex.connection.server.TCP;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import polimi.ingsw.am21.codex.controller.GameController;

public class TCPConnectionServer {

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
  private final Map<UUID, TCPConnectionHandler> activeHandlers;

  /**
   * The listener for the controller associated to this server
   */
  private final TCPControllerListener controllerListener;

  public TCPConnectionServer(Integer port, GameController controller) {
    this.port = port;
    this.controller = controller;
    this.activeHandlers = new HashMap<>();

    this.controllerListener = new TCPControllerListener(message -> {
      for (TCPConnectionHandler handler : activeHandlers.values()) {
        handler.send(message);
      }
    });
  }

  public void start() throws PortUnreachableException {
    controller.addListener(controllerListener);

    // Using try-with-resources here will automatically shut down the executor if
    // no longer needed, as it implements the AutoCloseable interface.
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      System.out.println("Starting TCP server on port " + port);

      ServerSocket serverSocket = new ServerSocket(port);
      System.out.println("TCP server ready on port " + port);
      while (true) {
        UUID socketId = UUID.randomUUID();
        try {
          Socket connectionSocket = serverSocket.accept();
          System.out.println(
            "Client connected from " + connectionSocket.getInetAddress()
          );

          TCPConnectionHandler handler = new TCPConnectionHandler(
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
      controller.removeListener(controllerListener);
    }
  }
}
