package polimi.ingsw.am21.codex;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.Server;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;

public class Main {

  private static void printHelp() {
    printAsciiArt();
    System.out.println("Usage: ");
    System.out.println("Server mode: ");
    System.out.println("java -jar codex.jar --server");
    System.out.println("Client mode: ");
    System.out.println("java -jar codex.jar");
    System.out.println("Client Parameters: ");
    System.out.println("--rmi: use RMI connection (by default it uses TCP)");
    System.out.println(
      "--port=<port>: specify the port to connect to " +
      "(default: " +
      ConnectionType.TCP.getDefaultPort() +
      " for TCP, " +
      ConnectionType.RMI.getDefaultPort() +
      " for RMI)"
    );
    System.out.println("Server Parameters: ");
    System.out.println(
      "--server: [REQUIRED] start as server (by default it starts as client)"
    );
    System.out.println(
      "--rmi-port=<port>: specify the RMI Server port " +
      "(default " +
      ConnectionType.RMI.getDefaultPort() +
      ")"
    );
    System.out.println(
      "--tcp-port=<port>: specify the TCP Server port " +
      "(default " +
      ConnectionType.TCP.getDefaultPort() +
      ")"
    );
    System.out.println("Common Parameters: ");
    System.out.println("--help: print this help message");
  }

  private static void startServer(Integer tcpPort, Integer rmiPort) {
    Server server = new Server(tcpPort, rmiPort);
    try {
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void startClient(ConnectionType connectionType, Integer port)
    throws MalformedURLException, NotBoundException, RemoteException {
    // TODO

    //    throw new UnsupportedOperationException("Not implemented yet");
    Registry registry = LocateRegistry.getRegistry(2024);
    RMIConnectionHandler handler = (RMIConnectionHandler) registry.lookup(
      "//127.0.0.1:" + port + "/RMIConnectionHandler"
    );

    try {
      handler.createGame("test", UUID.randomUUID(), 4);
    } catch (EmptyDeckException e) {
      throw new RuntimeException(e);
    }

    System.out.println(handler.getGames());
    //    System.out.println("Games: " + serverRMI.getGames().toString());
  }

  public static void main(String[] args)
    throws MalformedURLException, NotBoundException, RemoteException {
    //TODO: cli helper to handle all launching modes

    if (Arrays.asList(args).contains("--help")) {
      printHelp();
      return;
    }

    if (Arrays.asList(args).contains("--server")) {
      AtomicReference<Integer> tcpPort = new AtomicReference<>(
        ConnectionType.TCP.getDefaultPort()
      );

      Arrays.stream(args)
        .filter(arg -> arg.startsWith("--tcp-port"))
        .findFirst()
        .ifPresent(arg -> {
          tcpPort.set(Integer.parseInt(arg.split("=")[1]));
        });
      AtomicReference<Integer> rmiPort = new AtomicReference<>(
        ConnectionType.RMI.getDefaultPort()
      );

      Arrays.stream(args)
        .filter(arg -> arg.startsWith("--rmi-port"))
        .findFirst()
        .ifPresent(arg -> {
          rmiPort.set(Integer.parseInt(arg.split("=")[1]));
        });
      startServer(tcpPort.get(), rmiPort.get());
    } else {
      ConnectionType connectionType = ConnectionType.TCP;

      if (Arrays.asList(args).contains("--rmi")) {
        connectionType = ConnectionType.RMI;
      }

      AtomicReference<Integer> port = new AtomicReference<>(
        connectionType.getDefaultPort()
      );

      Arrays.stream(args)
        .filter(arg -> arg.startsWith("--port"))
        .findFirst()
        .ifPresent(arg -> {
          port.set(Integer.parseInt(arg.split("=")[1]));
        });

      startClient(connectionType, port.get());
    }
  }

  static void printAsciiArt() {
    System.out.println(
      "_________            .___              \n" +
      "\\_   ___ \\  ____   __| _/____ ___  ___ \n" +
      "/    \\  \\/ /  _ \\ / __ |/ __ \\\\  \\/  / \n" +
      "\\     \\___(  <_> ) /_/ \\  ___/ >    <  \n" +
      " \\______  /\\____/\\____ |\\___  >__/\\_ \\ \n" +
      "        \\/            \\/    \\/      \\/ "
    );
  }
}
