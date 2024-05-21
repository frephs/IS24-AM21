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

  Integer rmiPort;
  Integer tcpPort;

  public Server(Integer tcpPort, Integer rmiPort, GameController controller) {
    this.tcpPort = tcpPort;
    this.rmiPort = rmiPort;
    this.controller = controller;
    this.tcpServer = new TCPConnectionServer(tcpPort, controller);
    this.rmiServer = new RMIConnectionServer(rmiPort, controller);
  }

  public Server(Integer tcpPort, Integer rmiPort) {
    this(tcpPort, rmiPort, new GameController());
  }

  public void start()
    throws MalformedURLException, RemoteException, PortUnreachableException {
    System.out.println("Starting RMI server on port " + this.rmiPort);
    this.rmiServer.start();
    System.out.println("Starting TCP server on port " + this.tcpPort);
    this.tcpServer.start();
  }
}
