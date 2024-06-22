package polimi.ingsw.am21.codex.connection.server.RMI;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import polimi.ingsw.am21.codex.Main;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.server.AbstractServer;
import polimi.ingsw.am21.codex.controller.GameController;

public class RMIServer extends AbstractServer {

  public RMIServer(Integer port, GameController controller) {
    super(port, controller);
  }

  public void start()
    throws MalformedURLException, RemoteException, UnknownHostException {
    try { //special exception handler for registry creation
      System.setProperty(
        "java.rmi.server.hostname",
        InetAddress.getLocalHost().getHostName()
      );
      Registry registry = LocateRegistry.createRegistry(port);
      System.out.println("java RMI registry created.");
      RMIServerConnectionHandlerImpl handler =
        new RMIServerConnectionHandlerImpl(this.controller);

      registry.rebind("IS24-AM21-CODEX", handler);

      serverReadyLatch.countDown();
    } catch (RemoteException e) {
      //do nothing, error means registry already exists
      System.out.println("java RMI registry already exists.");
    } catch (UnknownHostException e) {
      System.out.println(
        "Error occurred while starting RMI server" + e.getMessage()
      );
      throw e;
    }
  }

  public void stop() {
    try {
      Registry registry = LocateRegistry.getRegistry(port);
      registry.unbind("IS24-AM21-CODEX");
      System.out.println("RMI Server stopped.");
    } catch (RemoteException | NotBoundException e) {
      System.out.println(
        "Error occurred while stopping RMI server: " + e.getMessage()
      );
    }
    port = null;
  }
}
