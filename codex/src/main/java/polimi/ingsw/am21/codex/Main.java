package polimi.ingsw.am21.codex;

import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.rmi.RemoteException;
import polimi.ingsw.am21.codex.connection.server.Server;

public class Main {

  public static void main(String[] args)
    throws MalformedURLException, PortUnreachableException, RemoteException {
    //TODO: cli helper to handle all launching modes
    Server server = new Server(null, 4567);
    server.start();
  }
}
