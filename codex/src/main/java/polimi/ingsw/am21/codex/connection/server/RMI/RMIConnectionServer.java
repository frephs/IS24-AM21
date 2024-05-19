package polimi.ingsw.am21.codex.connection.server.RMI;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import polimi.ingsw.am21.codex.ConnectionType;
import polimi.ingsw.am21.codex.controller.GameController;

public class RMIConnectionServer {

  GameController controller;
  Integer port;

  public RMIConnectionServer(Integer port, GameController controller) {
    this.controller = controller;
    this.port = port;
  }

  public void start() throws MalformedURLException, RemoteException {
    Integer port = ConnectionType.RMI.getDefaultPort();
    try { //special exception handler for registry creation
      Registry registry = LocateRegistry.createRegistry(port);
      System.out.println("java RMI registry created.");
      RMIConnectionHandlerImpl handler = new RMIConnectionHandlerImpl(
        new GameController()
      );

      // Bind this object instance to the name we want
      registry.rebind("//127.0.0.1:" + port + "/RMIConnectionHandler", handler);
    } catch (RemoteException e) {
      //do nothing, error means registry already exists
      System.out.println("java RMI registry already exists.");
    }
  }
}
