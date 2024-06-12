package polimi.ingsw.am21.codex.view.TUI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.state.ClientContext;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.ViewClient;

public class CliClient extends ViewClient {

  //TODO move context out of here into viewClient. It's not a concern of the cli

  Scanner scanner;
  Cli cli;

  public CliClient() {
    cli = Cli.getInstance();
    localModel = new LocalModelContainer(cli);
    scanner = new Scanner(System.in);
  }

  @Override
  public void start(ConnectionType connectionType, String address, int port) {
    super.start(connectionType, address, port);

    cli.postNotification(
      NotificationType.CONFIRM,
      "TUI is active, you can submit commands. Write help to see the commands available in your context."
    );

    initializeCommandHandlers();

    while (true) {
      try {
        cli.printPrompt();
        String line = scanner.nextLine().trim();
        String[] command = line.split(" ");

        Set<CommandHandler> matchingCommands = commandHandlers
          .stream()
          .filter(
            commandHandler ->
              commandHandler.getUsage().split(" ")[0].equals(command[0])
          )
          .collect(Collectors.toSet());
        if (!matchingCommands.isEmpty()) {
          Set<CommandHandler> matchingContext = matchingCommands
            .stream()
            .filter(
              commandHandler ->
                commandHandler.getContext() == ClientContext.MENU ||
                commandHandler.getContext() == localModel.getState()
            )
            .collect(Collectors.toSet());
          if (!matchingContext.isEmpty()) {
            Set<CommandHandler> matchingUsages = matchingContext
              .stream()
              .filter(commandHandler -> commandHandler.matchUsageString(line))
              .collect(Collectors.toSet());

            if (matchingUsages.isEmpty() && !line.isEmpty()) {
              matchingContext.forEach(
                commandHandler ->
                  cli.postNotification(
                    NotificationType.WARNING,
                    "Invalid command. You maybe looking for: " +
                    commandHandler.getUsage()
                  )
              );
            } else if (!line.isEmpty()) {
              try {
                matchingUsages.forEach(
                  commandHandler -> commandHandler.handle(line.split(" "))
                );
              } catch (Exception e) {
                cli.postNotification(
                  NotificationType.ERROR,
                  "An error occurred while executing the command. \n"
                );
                cli.displayException(e);
              }
            }
          } else {
            cli.postNotification(
              NotificationType.WARNING,
              "The command is not available in the current context"
            );
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
    private final ClientContext context;

    public CommandHandler(
      String usage,
      String description,
      ClientContext context
    ) {
      this.usage = usage;
      this.description = description;
      this.context = context;
    }

    public boolean matchUsageString(String input) {
      // TODO fix this regex
      String regex = usage
        .replaceAll("<[^>]+>", "\\\\S+")
        .replaceAll("\\[([^\\]]+)\\]", "(?:$1)?")
        .replaceAll("\\|", "|")
        .replaceAll(" ", "\\\\s+"); // Allow optional spaces

      Pattern pattern = Pattern.compile("^" + regex + "$");
      Matcher matcher = pattern.matcher(input);
      return matcher.matches();
    }

    public abstract void handle(String[] command);

    public String getUsage() {
      return usage;
    }

    public String getDescription() {
      return description;
    }

    public ClientContext getContext() {
      return context;
    }
  }

  private final List<CommandHandler> commandHandlers = new LinkedList<>();

  private void initializeCommandHandlers() {
    // TODO add optional arguments to usages

    commandHandlers.add(
      new CommandHandler("exit", "Exit the program", ClientContext.MENU) {
        @Override
        public void handle(String[] command) {
          cli.postNotification(NotificationType.CONFIRM, "Closing...");
          client.disconnect();
          scanner.close();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "reconnect",
        "Connect to the server",
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          client.connect();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "help",
        "Display available commands",
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          ArrayList<String> usages = new ArrayList<>();
          ArrayList<String> contexts = new ArrayList<>();
          ArrayList<String> descriptions = new ArrayList<>();
          commandHandlers.forEach(commandHandler -> {
            usages.add(commandHandler.getUsage());
            contexts.add(commandHandler.getContext().toString().toLowerCase());
            descriptions.add(commandHandler.getDescription());
          });
          cli.postNotification(
            NotificationType.RESPONSE,
            CliUtils.getTable(
              new String[] { "Command", "Context", "Description" },
              usages,
              contexts,
              descriptions
            )
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "list-games",
        "List available games",
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          client.listGames();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "join-game <game-id>",
        "Join a game",
        ClientContext.LIST
      ) {
        @Override
        public void handle(String[] command) {
          client.connectToGame(command[1]);
          // TODO printed lobby is outdated
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "show lobby",
        "List the available players and see their status",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          localModel.listLobbyPlayers();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "leave-game",
        "Leave the current game",
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          client.leaveGameLobby();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "create-game <game-id> <number-of-players>",
        "Create a new game",
        ClientContext.LIST
      ) {
        @Override
        public void handle(String[] command) {
          client.createGame(command[1], Integer.parseInt(command[2]));
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "create-game-join <game-id> <number-of-players>",
        "Create a new game and join it.",
        ClientContext.LIST
      ) {
        @Override
        public void handle(String[] command) {
          client.createAndConnectToGame(
            command[1],
            Integer.parseInt(command[2])
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "get-tokens",
        "Get the available tokens",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          client.showAvailableTokens();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "set-token <color>",
        "Set the token color",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          client.lobbySetToken(TokenColor.fromString(command[1]));
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "set-nickname <nickname>",
        "Set the nickname",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          client.lobbySetNickname(command[1]);
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "get-objectives",
        "Get the available objectives to choose from",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          client.getObjectiveCards();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "choose-objective <1|2>",
        "Choose the objective card",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          if (!List.of("1", "2").contains(command[1])) {
            // TODO Handle invalid command
            return;
          }

          client.lobbyChooseObjectiveCard(command[1].equals("1"));
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "get-starter-card",
        "Get the starter card to place",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          client.getStarterCard();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "choose-starter-card-side <front|back>",
        "Choose the starter card side",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          if (!List.of("front", "back").contains(command[1])) {
            // TODO Handle invalid command
            return;
          }

          client.lobbyJoinGame(
            command[1].equals("front") ? CardSideType.FRONT : CardSideType.BACK
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "show context",
        "Show the client current context",
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          cli.postNotification(
            NotificationType.RESPONSE,
            (localModel.getState() != ClientContext.LIST)
              ? "You are now in the " +
              localModel.getState().toString().toLowerCase()
              : "You have not joined any lobby or game yet"
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "chat <message>",
        "Broadcast a message",
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          client.sendChatMessage(
            new ChatMessage(localModel.getSocketID(), command[1])
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "chat <player> <message>",
        "Send a message to a player",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
          client.sendChatMessage(
            new ChatMessage(
              localModel.getLocalGameBoard().getPlayerNickname(),
              command[1],
              command[2]
            )
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "show <playerboard|leaderboard|card|hand|objective|pairs>",
        "Show game information",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
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
              // TODO right now, just go by id; in the future we can switch to something better

              // Parse the card id
              int cardId;
              try {
                cardId = Integer.parseInt(command[2]);
              } catch (NumberFormatException e) {
                // TODO Handle invalid command
                return;
              }

              // TODO find a way to display ANY possible card, not just the played/playable ones
              Card card = localModel
                .getLocalGameBoard()
                .getPlayers()
                .stream()
                .flatMap(player ->
                  Stream.concat(
                    // Get cards from the player hands
                    player.getHand().stream(),
                    // Get cards from the player boards
                    player.getPlayedCards().values().stream().map(Pair::getKey)
                  ))
                .filter(c -> c.getId() == cardId)
                .findFirst()
                .orElse(null);

              if (card == null) {
                cli.postNotification(
                  NotificationType.WARNING,
                  "Card not found"
                );
              } else {
                cli.drawCard(card);
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
            // TODO Handle invalid command
          }
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "place <hand number> <row> <column> <front|back>",
        "Place a card on the game board",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
          if (
            command.length < 5 ||
            !Stream.of(command[1], command[2], command[3]).allMatch(
              s -> s.matches("\\d+")
            ) ||
            !List.of("front", "back").contains(command[4])
          ) {
            // TODO Handle invalid command
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
            // TODO Handle invalid command
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
        "Draw a card",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
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
            // TODO Handle invalid command
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
      new CommandHandler(
        "draw",
        "Pass your turn when no cards to draw are available",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
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
