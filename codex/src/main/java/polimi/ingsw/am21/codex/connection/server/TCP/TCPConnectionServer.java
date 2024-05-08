package polimi.ingsw.am21.codex.connection.server.TCP;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.ServerSocket;
import java.net.Socket;
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

  public TCPConnectionServer(Integer port, GameController controller) {
    this.port = port;
    this.controller = controller;
  }

  public void start() throws PortUnreachableException {
    // Using try-with-resources here will automatically shut down the executor if
    // no longer needed, as it implements the AutoCloseable interface.
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      System.out.println("Starting TCP server on port " + port);

      try (ServerSocket serverSocket = new ServerSocket(port)) {
        System.out.println("TCP server ready on port " + port);
        while (true) {
          try {
            Socket connectionSocket = serverSocket.accept();
            System.out.println(
              "Client connected from " + connectionSocket.getInetAddress()
            );
            executor.execute(
              new TCPConnectionHandler(connectionSocket, controller, executor)
            );
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
      }
    } // No need to catch errors here
  }
}
