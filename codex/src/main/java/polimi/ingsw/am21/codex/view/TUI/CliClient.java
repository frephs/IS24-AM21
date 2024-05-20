package polimi.ingsw.am21.codex.view.TUI;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.LocalGameBoard;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPConnectionHandler;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;

public class CliClient {

  private LocalGameBoard game;
  private ClientContext context = ClientContext.LOBBY;

  Scanner scanner = new Scanner(System.in);

  void start(ConnectionType connectionType, String address, int port) {
    String line;
    String[] command;
    ClientConnectionHandler client;

    Cli cli = Cli.getInstance();

    client = new TCPConnectionHandler(cli, address, port);

    //    if (connectionType == ConnectionType.TCP) {
    //      client = new TCPConnectionHandler(cli, address, port);
    //    } else {
    // // TODO what's the registry?
    //      client = new RMIConnectionClient(address, port);
    //    }
    client.connect();

    ExecutorService executorService = Executors.newCachedThreadPool();

    while (true) {
      try {
        line = scanner.nextLine().trim();
        command = line.split(" ");

        switch (command[0]) {
          case "exit":
            cli.postNotification(NotificationType.CONFIRM, "Closing...");
            client.disconnect();
            scanner.close();
            break;
          case "help":
            // TODO
            break;
          case "list-games":
            // TODO
            break;
          case "join-game":
            // TODO
            break;
          case "leave-game":
            // TODO
            break;
          case "create-game":
            // TODO
            break;
          case "get-tokens":
            // TODO
            break;
          case "set-token":
            // TODO
            break;
          case "set-nickname":
            // TODO
            break;
          case "choose-objective":
            // TODO
            break;
          case "start-game":
            // TODO
            break;
          case "show":
            // TODO board, card at position, hand, objective, pairs
            break;
          case "place":
            // TODO
            break;
          case "draw":
            // TODO (counts as next turn action)
            break;
          default:
            cli.postNotification(NotificationType.ERROR, "Invalid command");
        }
      } catch (IllegalStateException e) {
        // Scanner was closed
        break;
      } catch (NoSuchElementException ignored) {}
    }

    System.exit(0);
  }

  public static void main(String[] args) {
    if (args.length != 3) throw new IllegalArgumentException(
      "Usage: CliClient <connection-type> <address> <port>"
    );

    CliClient cliClient = new CliClient();

    // TODO add defaults from config file
    cliClient.start(
      Objects.equals(args[0], "--TCP")
        ? ConnectionType.TCP
        : ConnectionType.RMI,
      args[1] != null ? args[1] : "localhost",
      args[2] != null ? Integer.parseInt(args[2]) : 12345
    );
  }
}
