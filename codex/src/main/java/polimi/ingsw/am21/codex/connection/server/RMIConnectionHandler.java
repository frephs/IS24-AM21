package polimi.ingsw.am21.codex.connection.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public class RMIConnectionHandler implements Remote {

  public RMIConnectionHandler() throws RemoteException {}

  public String sayHello(String name) throws RemoteException {
    return "Hello, " + name + "!";
  }
}
