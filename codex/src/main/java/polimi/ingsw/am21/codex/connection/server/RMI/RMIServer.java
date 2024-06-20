package polimi.ingsw.am21.codex.connection.server.RMI;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.controller.GameController;

public class RMIServer {

  GameController controller;
  Integer port;

  public RMIServer(Integer port, GameController controller) {
    this.controller = controller;
    this.port = port;
  }

  public void start() throws MalformedURLException, RemoteException {
    int port = ConnectionType.RMI.getDefaultPort();
    try { //special exception handler for registry creation
      Registry registry = LocateRegistry.createRegistry(port);
      System.out.println("java RMI registry created.");
      RMIServerConnectionHandlerImpl handler =
        new RMIServerConnectionHandlerImpl(this.controller);

      // Bind this object instance to the name we want
      registry.rebind("//127.0.0.1:" + port + "/IS24-AM21-CODEX", handler);
    } catch (RemoteException e) {
      //do nothing, error means registry already exists
      System.out.println("java RMI registry already exists.");
    }
  }

  public void stop() {
    try {
      Registry registry = LocateRegistry.getRegistry(port);
      registry.unbind("//127.0.0.1:" + port + "/IS24-AM21-CODEX");
      System.out.println("RMI Server stopped.");
    } catch (RemoteException | NotBoundException e) {
      System.out.println(
        "Error occurred while stopping RMI server: " + e.getMessage()
      );
    }
    port = null;
  }
}
