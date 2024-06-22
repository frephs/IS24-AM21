package polimi.ingsw.am21.codex.connection.server;

import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIServer;
import polimi.ingsw.am21.codex.connection.server.TCP.TCPServer;
import polimi.ingsw.am21.codex.controller.GameController;

public class Server {

  GameController controller;
  RMIServer rmiServer;
  TCPServer tcpServer;

  Integer rmiPort;
  Integer tcpPort;

  public Server(Integer tcpPort, Integer rmiPort, GameController controller) {
    this.tcpPort = tcpPort;
    this.rmiPort = rmiPort;
    this.controller = controller;
    this.tcpServer = new TCPServer(tcpPort, controller);
    this.rmiServer = new RMIServer(rmiPort, controller);
  }

  public Server(Integer tcpPort, Integer rmiPort) {
    this(tcpPort, rmiPort, new GameController());
  }

  public void start()
    throws MalformedURLException, RemoteException, PortUnreachableException, UnknownHostException, AlreadyBoundException {
    System.out.println("Starting RMI server on port " + this.rmiPort);
    this.rmiServer.start();
    System.out.println("Starting TCP server on port " + this.tcpPort);
    this.tcpServer.start();
  }

  public void stop() {
    //TODO use stop method when keyboard interrupt is received
    this.rmiServer.stop();
    this.tcpServer.stop();
  }
}
