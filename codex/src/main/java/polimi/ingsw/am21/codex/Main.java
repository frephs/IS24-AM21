package polimi.ingsw.am21.codex;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import polimi.ingsw.am21.codex.client.ClientType;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.server.Server;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.view.GUI.GuiClient;
import polimi.ingsw.am21.codex.view.TUI.CliClient;
import polimi.ingsw.am21.codex.view.View;
import polimi.ingsw.am21.codex.view.ViewClient;

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

  private static void startClient(
    String serverAddress,
    ClientType clientType,
    ConnectionType connectionType,
    Integer port
  ) throws MalformedURLException, NotBoundException, RemoteException {
    System.out.println(
      "Starting client " +
      clientType +
      " on " +
      connectionType +
      " to " +
      serverAddress +
      ":" +
      port
    );
    View view;

    ViewClient client;
    if (clientType == ClientType.CLI) {
      client = new CliClient();
    } else {
      client = new GuiClient();
    }

    client.start(connectionType, serverAddress, port);
  }

  public static void main(String[] args)
    throws MalformedURLException, NotBoundException, RemoteException {
    //TODO: cli helper to handle all launching modes

    //    printAsciiArt();

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
      ClientType clientType = ClientType.GUI;
      AtomicReference<String> serverAddress = new AtomicReference<>(
        "127.0.0.1"
      );

      if (Arrays.asList(args).contains("--cli")) {
        clientType = ClientType.CLI;
      }

      if (Arrays.asList(args).contains("--rmi")) {
        connectionType = ConnectionType.RMI;
      }

      AtomicReference<Integer> port = new AtomicReference<>(
        connectionType.getDefaultPort()
      );

      Arrays.stream(args)
        .filter(arg -> arg.startsWith("--ip"))
        .findFirst()
        .ifPresent(arg -> {
          serverAddress.set(arg.split("=")[1]);
        });

      Arrays.stream(args)
        .filter(arg -> arg.startsWith("--port"))
        .findFirst()
        .ifPresent(arg -> {
          port.set(Integer.parseInt(arg.split("=")[1]));
        });

      startClient(serverAddress.get(), clientType, connectionType, port.get());
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
