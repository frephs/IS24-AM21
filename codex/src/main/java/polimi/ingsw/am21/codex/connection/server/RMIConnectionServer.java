package polimi.ingsw.am21.codex.connection.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import polimi.ingsw.am21.codex.controller.GameController;

public class RMIConnectionServer {

  GameController controller;
  String host;
  Integer port;

  public RMIConnectionServer(
    String host,
    Integer port,
    GameController controller
  ) {
    this.controller = controller;
    this.host = host;
    this.port = port;
  }

  void start() throws MalformedURLException, RemoteException {
    System.out.println("RMI server started");

    try { //special exception handler for registry creation
      LocateRegistry.createRegistry(this.port);
      System.out.println("java RMI registry created.");
    } catch (RemoteException e) {
      //do nothing, error means registry already exists
      System.out.println("java RMI registry already exists.");
    }

    //Instantiate RmiServer
    RMIConnectionHandler handler = new RMIConnectionHandler(controller);

    // Bind this object instance to the name "RmiServer"
    Naming.rebind(this.host, handler);
  }
}
