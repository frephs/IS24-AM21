package polimi.ingsw.am21.codex.view.TUI;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.Main;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.LocalGameBoard;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
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

  Scanner scanner;
  Cli cli;

  public CliClient() {
    super(new Cli());
    cli = (Cli) view;
    scanner = new Scanner(System.in);
  }

  private LocalModelContainer.ClientContextContainer getClientContextContainer() {
    return gameEventHandler.getLocalModel().getClientContextContainer();
  }

  @Override
  public void start(
    ConnectionType connectionType,
    String address,
    int port,
    UUID connectionID
  ) {
    super.start(connectionType, address, port, connectionID);
    cli.setClient(client);

    cli.postNotification(
      NotificationType.CONFIRM,
      "TUI is active, you can submit commands. Write help to see the commands available in your context."
    );

    initializeCommandHandlers();

    while (true) {
      try {
        cli.printPrompt();
        if (
          cli
            .getLocalModel()
            .getLocalGameBoard()
            .map(LocalGameBoard::isHalted)
            .orElse(false)
        ) {
          cli.postNotification(
            NotificationType.WARNING,
            "The game has been halted, wait for the game to resume"
          );
        }
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
                commandHandler
                  .getContext()
                  .map(
                    commandContext ->
                      commandContext == ClientContext.ALL ||
                      commandContext.equals(
                        getClientContextContainer().get().get()
                      )
                  )
                  .orElse(false)
            )
            .collect(Collectors.toSet());
          if (!matchingContext.isEmpty()) {
            Set<CommandHandler> matchingUsages = matchingContext
              .stream()
              .filter(
                commandHandlers ->
                  commandHandlers.matchUsageString(line) ||
                  commandHandlers.skipUsageCheck
              )
              .collect(Collectors.toSet());

            if (matchingUsages.isEmpty() && !line.isEmpty()) {
              matchingContext.forEach(
                commandHandler ->
                  cli.postNotification(
                    NotificationType.WARNING,
                    "Invalid usage. You may try with: " +
                    commandHandler.getUsage()
                  )
              );
            } else if (!line.isEmpty()) {
              try {
                matchingUsages.forEach(
                  commandHandlers -> commandHandlers.handle(line.split(" "))
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
              "The command is not available in the current context (" +
              getClientContextContainer().get().get().toString() +
              "), you can use it only in " +
              matchingCommands
                .stream()
                .map(
                  commandHandler -> commandHandler.getContext().get().toString()
                )
                .collect(Collectors.joining(", "))
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
    private Boolean skipUsageCheck = false;

    public CommandHandler(
      String usage,
      String description,
      ClientContext context
    ) {
      this.usage = usage;
      this.description = description;
      this.context = context;
    }

    public CommandHandler(
      String usage,
      String description,
      ClientContext context,
      Boolean skipUsageCheck
    ) {
      this(usage, description, context);
      this.skipUsageCheck = skipUsageCheck;
    }

    public boolean matchUsageString(String input) {
      // TODO fix this regex
      String regex = usage
        .replaceAll("<[^>]+>", "\\\\S+")
        .replaceAll("\\[([^]]+)]", "(?:$1)?")
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

    public Optional<ClientContext> getContext() {
      return Optional.ofNullable(context);
    }
  }

  private final List<CommandHandler> commandHandlers = new LinkedList<>();

  private void initializeCommandHandlers() {
    // TODO add optional arguments to usages

    final LocalModelContainer.ClientContextContainer currentContext =
      getClientContextContainer();

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
        ClientContext.ALL
      ) {
        @Override
        public void handle(String[] command) {
          client.connect();
          currentContext.set(null);
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "help",
        "Display available commands",
        ClientContext.ALL
      ) {
        @Override
        public void handle(String[] command) {
          ArrayList<String> usages = new ArrayList<>();
          ArrayList<String> contexts = new ArrayList<>();
          ArrayList<String> descriptions = new ArrayList<>();
          commandHandlers
            .stream()
            .filter(
              commandHandler ->
                currentContext
                  .get()
                  .map(
                    currentContextVal ->
                      commandHandler
                        .getContext()
                        .map(
                          context ->
                            context == ClientContext.ALL ||
                            context.equals(currentContextVal)
                        )
                        .orElse(true)
                  )
                  .orElse(true)
            )
            .sorted(
              Comparator.comparing(
                commandHandler ->
                  commandHandler.getContext().orElse(ClientContext.ALL)
              )
            )
            .forEach(commandHandler -> {
              usages.add(commandHandler.getUsage());
              contexts.add(
                commandHandler.getContext().get().toString().toLowerCase()
              );
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
        "show games",
        "List available games",
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          client.getGames();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "join-game <game-id>",
        "Join a game",
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          client.connectToGame(command[1]);
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
          view.drawLobby();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "leave-lobby",
        "Leave the current lobby",
        ClientContext.LOBBY
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
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          try {
            client.createGame(command[1], Integer.parseInt(command[2]));
          } catch (NumberFormatException e) {
            cli.postNotification(
              NotificationType.ERROR,
              "Invalid number of players"
            );
          }
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "create-game-join <game-id> <number-of-players>",
        "Create a new game and join it.",
        ClientContext.MENU
      ) {
        @Override
        public void handle(String[] command) {
          try {
            client.createAndConnectToGame(
              command[1],
              Integer.parseInt(command[2])
            );
          } catch (NumberFormatException e) {
            cli.postNotification(
              NotificationType.ERROR,
              "Invalid number of players"
            );
          }
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
          cli.drawAvailableTokenColors();
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
          view.drawObjectiveCardChoice();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "set-objective <1|2>",
        "Choose the objective card",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          if (!List.of("1", "2").contains(command[1])) {
            // TODO Handle invalid command
            return;
          }
          client.getObjectivesIfNull();
          client.lobbyChooseObjectiveCard(command[1].equals("1"));
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "get-starter",
        "Get the starter card to place",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          view.drawStarterCardSides();
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "set-starter <front|back>",
        "Choose the starter card side",
        ClientContext.LOBBY
      ) {
        @Override
        public void handle(String[] command) {
          if (!List.of("front", "back").contains(command[1])) {
            view.postNotification(NotificationType.ERROR, "Invalid side");
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
        ClientContext.ALL
      ) {
        @Override
        public void handle(String[] command) {
          cli.postNotification(
            NotificationType.RESPONSE,
            currentContext
              .get()
              .map(
                context ->
                  "You are now in the " + context.toString().toLowerCase()
              )
              .orElse("You have not joined any lobby or game yet")
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "chat <message>",
        "Broadcast a message",
        ClientContext.GAME,
        true
      ) {
        @Override
        public void handle(String[] command) {
          client.sendChatMessage(
            new ChatMessage(
              view
                .getLocalModel()
                .getLocalGameBoard()
                .get()
                .getPlayerNickname(),
              Arrays.stream(command).skip(1).collect(Collectors.joining(" "))
            )
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "whisper <player> <message>",
        "Send a message to a player",
        ClientContext.GAME,
        true
      ) {
        @Override
        public void handle(String[] command) {
          if (command.length < 2) {
            cli.postNotification(
              NotificationType.ERROR,
              "Specify the player you want to whisper to"
            );
            return;
          }
          if (
            !(view
                .getLocalModel()
                .getLocalGameBoard()
                .get()
                .getPlayers()
                .stream()
                .filter(
                  localPlayer -> localPlayer.getNickname().equals(command[1])
                )
                .collect(Collectors.toSet())
                .isEmpty())
          ) {
            client.sendChatMessage(
              new ChatMessage(
                view
                  .getLocalModel()
                  .getLocalGameBoard()
                  .orElseThrow()
                  .getPlayerNickname(),
                command[1],
                Arrays.stream(command).skip(2).collect(Collectors.joining(" "))
              )
            );
          } else {
            cli.postNotification(NotificationType.ERROR, "Player not found");
            cli.postNotification(
              NotificationType.WARNING,
              "Usage: " + this.getUsage()
            );
          }
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "show playerboard <nickname>",
        "Show the playerboard of the given player",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
          view
            .getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getPlayerByNickname(command[2])
            .ifPresentOrElse(
              player -> cli.drawPlayerBoard(command[2]),
              () ->
                cli.postNotification(
                  NotificationType.WARNING,
                  "Player not found"
                )
            );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "show playerboard <nickname> <^V<>>",
        "Show the playerboard of the given player, navigating vertically and horizontally",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
          int verticalOffset = 0;
          int horizontalOffset = 0;

          for (int i = 0; i < command[3].length(); i++) {
            if (command[3].charAt(i) == '^') {
              verticalOffset--;
            } else if (command[3].charAt(i) == 'V') {
              verticalOffset++;
            } else if (command[3].charAt(i) == '<') {
              horizontalOffset--;
            } else if (command[3].charAt(i) == '>') {
              horizontalOffset++;
            }
          }

          int finalVerticalOffset = verticalOffset;
          int finalHorizontalOffset = horizontalOffset;
          view
            .getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getPlayerByNickname(command[2])
            .ifPresentOrElse(
              player ->
                cli.drawPlayerBoard(
                  command[2],
                  finalVerticalOffset,
                  finalHorizontalOffset
                ),
              () ->
                cli.postNotification(
                  NotificationType.WARNING,
                  "Player not found"
                )
            );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "show card <id>",
        "Show the card of the given id",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
          // TODO right now, just go by id; in the future we can switch to something better

          // Parse the card id
          int cardId;
          try {
            cardId = Integer.parseInt(command[2]);
          } catch (NumberFormatException e) {
            cli.postNotification(NotificationType.WARNING, "Invalid card id");
            return;
          }

          Card card = view
            .getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getPlayers()
            .stream()
            .flatMap(player ->
              Stream.concat(
                // Get cards from the player hands
                player.getHand().stream(),
                // Get cards from the player boards
                player
                  .getPlayedCardsByPosition()
                  .values()
                  .stream()
                  .map(Pair::getKey)
              ))
            .filter(c -> c.getId() == cardId)
            .findFirst()
            .orElse(null);

          if (card == null) {
            cli.postNotification(NotificationType.WARNING, "Card not found");
          } else {
            cli.drawCard(card);
          }
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "show <playerboard|leaderboard|hand|secret-objective|common-objectives|pairs|decks>",
        "Show game information",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
          switch (command[1]) {
            case "playerboard":
              view.drawPlayerBoard();
              break;
            case "leaderboard":
              cli.drawLeaderBoard();
              break;
            case "hand":
              cli.drawHand();
              break;
            case "secret-objective":
              cli.drawCard(
                view
                  .getLocalModel()
                  .getLocalGameBoard()
                  .orElseThrow()
                  .getPlayer()
                  .getObjectiveCard()
              );
              break;
            case "pairs":
              cli.drawPairs();
              break;
            case "decks":
              cli.drawCardDecks();
              break;
            case "common-objectives":
              cli.drawCommonObjectiveCards();
              break;
            default:
              cli.postNotification(NotificationType.WARNING, "Invalid command");
              break;
          }
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "place <hand number> <x> <y> <front|back>",
        "Place a card on the game board",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
          if (
            command.length < 5 ||
            !Stream.of(command[1], command[2], command[3]).allMatch(
              s -> s.matches("-?\\d+")
            ) ||
            !List.of("front", "back").contains(command[4])
          ) {
            view.postNotification(
              NotificationType.ERROR,
              "Invalid card place call"
            );
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
            view.postNotification(
              NotificationType.WARNING,
              "Invalid number format"
            );
            return;
          }
          client.placeCard(
            handIndex - 1,
            CardSideType.valueOf(command[4].toUpperCase()),
            position
          );
        }
      }
    );

    commandHandlers.add(
      new CommandHandler(
        "draw deck <resource|gold>",
        "Draw a card",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
          if (
            command.length < 3 ||
            !List.of("draw", "deck", "resource", "gold").contains(command[1])
          ) {
            view.postNotification(NotificationType.WARNING, "Invalid command");
            return;
          }

          DrawingCardSource source = DrawingCardSource.Deck;
          DrawingDeckType type = command[2].equals("resource")
            ? DrawingDeckType.RESOURCE
            : DrawingDeckType.GOLD;
          client.nextTurn(source, type);
        }
      }
    );
    commandHandlers.add(
      new CommandHandler(
        "draw pair <resource1|resource2|gold1|gold2>",
        "Draw a card",
        ClientContext.GAME
      ) {
        @Override
        public void handle(String[] command) {
          DrawingCardSource source = command[2].endsWith("1")
            ? DrawingCardSource.CardPairFirstCard
            : DrawingCardSource.CardPairSecondCard;
          DrawingDeckType type = command[2].split("1")[0].split("2")[0].equals(
                "resource"
              )
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
    if (
      args.length != 3 && args.length != 4
    ) throw new IllegalArgumentException(
      "Usage: CliClient <connection-type> <address> <port> ?<your-previous-id>?"
    );

    new Main.Options(true);
    new Cli.Options(true);

    CliClient cliClient = new CliClient();

    // TODO add defaults from config file
    try {
      ConnectionType connectionType = Objects.equals(args[0], "--TCP")
        ? ConnectionType.TCP
        : ConnectionType.RMI;
      cliClient.start(
        connectionType,
        args[1] != null ? args[1] : "localhost",
        args[2] != null
          ? Integer.parseInt(args[2])
          : connectionType.getDefaultPort(),
        Optional.ofNullable(args.length == 4 ? args[3] : null)
          .map(UUID::fromString)
          .orElse(UUID.randomUUID())
      );
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid port number");
    }
  }
}
