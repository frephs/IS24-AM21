package polimi.ingsw.am21.codex.connection.client.RMI;

import polimi.ingsw.am21.codex.connection.server.RMI.RMIConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIConnectionServer;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIConnectionClient{
  private final String host;
  private final Integer port;
  Registry registry;

  public RMIConnectionClient(String host, Integer port, Registry registry) {
    this.host = host;
    this.port = port;
    this.registry = registry;
  }

  public void start() {
    try {
      registry = LocateRegistry.getRegistry(host);
      RMIConnectionHandler rmiConnectionHandler = (RMIConnectionHandler) Naming.lookup("");

      RMIClientConnectionHandler handler = new RMIClientConnectionHandler(rmiConnectionHandler);

      Naming.bind(this.host, handler);
    } catch (Exception e) {
        System.err.println("erroreee");
    }
  }

}
