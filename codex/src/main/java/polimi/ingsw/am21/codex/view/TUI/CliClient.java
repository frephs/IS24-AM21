package polimi.ingsw.am21.codex.view.TUI;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.RMI.RMIClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPClientConnectionHandler;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;

public class CliClient {

  private LocalModelContainer localModel;
  private ClientContext context = ClientContext.LOBBY;

  Scanner scanner = new Scanner(System.in);
  Cli cli;
  ClientConnectionHandler client;

  public void start(ConnectionType connectionType, String address, int port) {
    ClientConnectionHandler client;

    cli = Cli.getInstance();

    if (connectionType == ConnectionType.TCP) {
      client = new TCPClientConnectionHandler(cli, address, port);
    } else {
      client = new RMIClientConnectionHandler(cli, address, port);
    }

    initializeCommandHandlers();

    while (true) {
      try {
        System.out.print("Enter a command:\n > ");
        String line = scanner.nextLine().trim();
        String[] command = line.split(" ");

        Set<CommandHandler> matchingCommands = commandHandlers
          .stream()
          .filter(
            commandHandler -> commandHandler.getUsage().startsWith(command[0])
          )
          .collect(Collectors.toSet());

        if (!matchingCommands.isEmpty()) {
          Set<CommandHandler> matchingUsages = matchingCommands
            .stream()
            .filter(commandHandlers -> commandHandlers.matchUsageString(line))
            .collect(Collectors.toSet());
          if (matchingUsages.isEmpty() && !line.isEmpty()) {
            matchingCommands.forEach(
              commandHandler ->
                cli.postNotification(
                  NotificationType.WARNING,
                  "Invalid command. You maybe looking for: " +
                  commandHandler.getUsage()
                )
            );
          } else if (!line.isEmpty()) {
            try {
              matchingCommands.forEach(
                commandHandlers ->
                  commandHandlers.handle(
                    line.split(" "),
                    cli,
                    scanner,
                    client,
                    localModel
                  )
              );
            } catch (Exception e) {
              cli.postNotification(
                NotificationType.ERROR,
                "An error occurred while executing the command. \n" +
                e.getMessage()
              );
            }
          }
        } else {
          cli.postNotification(
            NotificationType.WARNING,
            "Unknown command. Use help to display the available commands"
          );
        }
      } catch (IllegalStateException e) {
        // Scanner was closed
        System.out.println("Scanner was closed");
        break;
      } catch (NoSuchElementException ignored) {}
    }

    System.exit(0);
  }

  private abstract static class CommandHandler {

    private final String usage;
    private final String description;

    public CommandHandler(String usage, String description) {
      this.usage = usage;
      this.description = description;
    }

    public boolean matchUsageString(String input) {
      String regex = usage
        .replaceAll("<[^>]+>", "\\\\S+")
        .replaceAll("\\[([^\\]]+)\\]", "(?:$1)?")
        .replaceAll("\\|", "|")
        .replaceAll(" ", "\\\\s+"); // Allow optional spaces

      Pattern pattern = Pattern.compile("^" + regex + "$");
      Matcher matcher = pattern.matcher(input);
      return matcher.matches();
    }

    public abstract void handle(
      String[] command,
      Cli cli,
      Scanner scanner,
      ClientConnectionHandler client,
      LocalModelContainer localModel
    );

    public String getUsage() {
      return usage;
    }

    public String getDescription() {
      return description;
    }
  }

  private final List<CommandHandler> commandHandlers = new LinkedList<>();

  private void initializeCommandHandlers() {
    commandHandlers.add(
      new CommandHandler("exit", "Exit the program") {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          cli.postNotification(NotificationType.CONFIRM, "Closing...");
          client.disconnect();
          scanner.close();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler("help", "Display available commands") {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          List<String> commands = new ArrayList<>();
          List<String> usages = new ArrayList<>();
          List<String> descriptions = new ArrayList<>();
          commandHandlers.forEach(commandHandler -> {
            usages.add(commandHandler.getUsage());
            descriptions.add(commandHandler.getDescription());
          });
          cli.postNotification(
            NotificationType.RESPONSE,
            CliUtils.getTable(
              new String[] { "Command", "Description" },
              usages,
              descriptions
            )
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler("list-games", "List available games") {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          // TODO implement list-games command
        }
      }
    );

    commandHandlers.add(
      new CommandHandler("join-game <game-id>", "Join a game") {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          client.connectToGame(command[1]);
        }
      }
    );

    commandHandlers.add(
      new CommandHandler("leave-game", "Leave the current game") {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          client.leaveGameLobby();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "create-game <game-id> <number-of-players>",
        "Create a new game"
      ) {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          client.createAndConnectToGame(
            command[1],
            Integer.parseInt(command[2])
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler("get-tokens", "Get the available tokens") {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          // TODO implement get-tokens command
        }
      }
    );

    commandHandlers.add(
      new CommandHandler("set-token <color>", "Set the token color") {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          client.lobbySetToken(TokenColor.fromString(command[1]));
        }
      }
    );

    commandHandlers.add(
      new CommandHandler("set-nickname <nickname>", "Set the nickname") {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          client.lobbySetNickname(command[1]);
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "choose-objective <1|2>",
        "Choose the objective card"
      ) {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          client.lobbyChooseObjectiveCard(command[1].equals("1"));
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "choose-starter-card-side <front|back>",
        "Choose the starter card side"
      ) {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          client.lobbyJoinGame(
            command[1].equals("front") ? CardSideType.FRONT : CardSideType.BACK
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "show <playerboard|leaderboard|card|hand|objective|pairs>",
        "Show game information"
      ) {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          switch (command[1]) {
            case "playerboard":
              final String nickname = command[2];
              cli.drawPlayerBoard(
                localModel
                  .getLocalGameBoard()
                  .getPlayers()
                  .stream()
                  .filter(player -> player.getNickname().equals(nickname))
                  .findFirst()
                  .orElse(localModel.getLocalGameBoard().getPlayer())
              );
              break;
            case "leaderboard":
              cli.drawLeaderBoard(localModel.getLocalGameBoard().getPlayers());
              break;
            case "card":
              Position pos = new Position();
              try {
                // TODO handle position parsing
              } catch (NumberFormatException e) {
                // Handle invalid command
                return;
              }
              Pair<Card, CardSideType> entry = localModel
                .getLocalGameBoard()
                .getPlayer()
                .getPlayedCards()
                .get(pos);
              if (entry != null) {
                // TODO handle card display
              }
              break;
            case "hand":
              cli.drawHand(
                localModel.getLocalGameBoard().getPlayer().getHand()
              );
              break;
            case "objective":
              cli.drawCard(localModel.getLocalGameBoard().getSecretObjective());
              break;
            case "pairs":
              cli.drawPairs(
                localModel.getLocalGameBoard().getResourceCards(),
                localModel.getLocalGameBoard().getGoldCards()
              );
              break;
            default:
              // Handle invalid command
              return;
          }
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "place <hand number> <row> <column> <front|back>",
        "Place a card on the game board"
      ) {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          if (
            command.length < 5 ||
            !Stream.of(command[1], command[2], command[3]).allMatch(
              s -> s.matches("\\d+")
            ) ||
            !List.of("front", "back").contains(command[4])
          ) {
            // Handle invalid command
            return;
          }
          int handIndex;
          Position position;
          try {
            handIndex = Integer.parseInt(command[1]);
            position = new Position(
              Integer.parseInt(command[2]),
              Integer.parseInt(command[3])
            );
          } catch (NumberFormatException e) {
            // Handle invalid command
            return;
          }
          client.placeCard(
            handIndex,
            CardSideType.valueOf(command[4].toUpperCase()),
            position
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "draw <deck|resource1|resource2|gold1|gold2>",
        "Draw a card"
      ) {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          if (
            command.length < 2 ||
            !List.of(
              "deck",
              "resource1",
              "resource2",
              "gold1",
              "gold2"
            ).contains(command[1])
          ) {
            // Handle invalid command
            return;
          }

          DrawingCardSource source = command[1].equals("deck")
            ? DrawingCardSource.Deck
            : (command[1].endsWith("1")
                ? DrawingCardSource.CardPairFirstCard
                : DrawingCardSource.CardPairSecondCard);
          DrawingDeckType type = command[1].startsWith("resource")
            ? DrawingDeckType.RESOURCE
            : DrawingDeckType.GOLD;
          client.nextTurn(source, type);
        }
      }
    );

    commandHandlers.add(
      new CommandHandler("draw", "Pass your turn when no cards are available") {
        @Override
        public void handle(
          String[] command,
          Cli cli,
          Scanner scanner,
          ClientConnectionHandler client,
          LocalModelContainer localModel
        ) {
          client.nextTurn();
        }
      }
    );
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
