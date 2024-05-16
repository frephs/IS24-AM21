package polimi.ingsw.am21.codex.connection.server;

import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.rmi.RemoteException;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIConnectionServer;
import polimi.ingsw.am21.codex.connection.server.TCP.TCPConnectionServer;
import polimi.ingsw.am21.codex.controller.GameController;

public class Server {

  GameController controller;
  RMIConnectionServer rmiServer;
  TCPConnectionServer tcpServer;

  public Server(
    String rmiHost,
    Integer rmiPort,
    Integer tcpPort,
    GameController controller
  ) {
    this.controller = controller;
    //    this.rmiServer = new RMIConnectionServer(rmiHost, rmiPort, controller);
    this.tcpServer = new TCPConnectionServer(tcpPort, controller);
  }

  public Server(String rmiHost, Integer rmiPort, Integer tcpPort) {
    this(rmiHost, rmiPort, tcpPort, new GameController());
  }

  public Server(Integer rmiPort, Integer tcpPort) {
    this("//localhost/CodexServer", rmiPort, tcpPort);
  }

  public void start()
    throws MalformedURLException, RemoteException, PortUnreachableException {
    //    this.rmiServer.start();
    this.tcpServer.start();
  }
}
