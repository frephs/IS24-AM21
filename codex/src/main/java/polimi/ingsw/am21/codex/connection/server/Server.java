package polimi.ingsw.am21.codex.connection.server;

import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.rmi.RemoteException;
import polimi.ingsw.am21.codex.controller.GameController;

public class Server {

  GameController controller;
  RMIConnectionServer rmiServer;
  TCPConnectionServer tcpServer;

  public Server(String rmiHost, Integer port, GameController controller) {
    this.controller = controller;
    this.rmiServer = new RMIConnectionServer(rmiHost, port, controller);
    this.tcpServer = new TCPConnectionServer(port, controller);
  }

  public Server(Integer port, String rmiHost) {
    this(rmiHost, port, new GameController());
  }

  public Server(Integer port) {
    this(port, "//localhost/CodexServer");
  }

  public void start()
    throws MalformedURLException, RemoteException, PortUnreachableException {
    this.rmiServer.start();
    this.tcpServer.start();
  }
}
