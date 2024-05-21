package polimi.ingsw.am21.codex.view.TUI;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.LocalGameBoard;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPConnectionHandler;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;
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

        // TODO add lobby checks ???
        switch (command[0]) {
          case "exit":
            cli.postNotification(NotificationType.CONFIRM, "Closing...");
            client.disconnect();
            scanner.close();
            break;
          case "help":
            cli.postNotification(
              NotificationType.RESPONSE,
              """
              Available commands:
              list-games
              join-game <game-id>
              leave-game
              create-game <game-id> <number-of-players>
              get-tokens
              set-token <color>
              set-nickname <nickname>
              choose-objective <1|2>
              start-game <front|back>
              show <playerboard|leaderboard|card|hand|objective|pairs>
              place <hand number> <row> <column> <front|back>
              draw [deck|resource1|resource2|gold1|gold2]"""
            );
            break;
          case "list-games":
            client.getGames();
            break;
          case "join-game":
            if (command[1] == null) {
              cli.postNotification(
                NotificationType.ERROR,
                "Invalid command. Usage: join-game <game-id>"
              );
              break;
            }
            client.connectToGame(command[1]);
            break;
          case "leave-game":
            client.leaveGameLobby();
            break;
          case "create-game":
            if (command[1] == null || command[2] == null) {
              cli.postNotification(
                NotificationType.ERROR,
                "Invalid command. Usage: create-game <game-id> <number-of-players>"
              );
              break;
            }
            client.createAndConnectToGame(
              command[1],
              Integer.parseInt(command[2])
            );
            break;
          case "get-tokens":
            // TODO what should it do?
            break;
          case "set-token":
            if (
              command[1] == null || TokenColor.fromString(command[1]) == null
            ) {
              cli.postNotification(
                NotificationType.ERROR,
                "Invalid command. Usage: set-token <color>"
              );
              break;
            }

            client.lobbySetToken(TokenColor.fromString(command[1]));
            break;
          case "set-nickname":
            if (command[1] == null) {
              cli.postNotification(
                NotificationType.ERROR,
                "Invalid command. Usage: set-nickname <nickname>"
              );
              break;
            }

            client.lobbySetNickname(command[1]);
            break;
          case "choose-objective":
            if (!List.of("1", "2").contains(command[1])) {
              cli.postNotification(
                NotificationType.ERROR,
                "Invalid command. Usage: choose-objective <1|2>"
              );
              break;
            }

            client.lobbyChooseObjectiveCard(command[1].equals("1"));
            break;
          case "start-game":
            if (!List.of("front", "back").contains(command[1])) {
              cli.postNotification(
                NotificationType.ERROR,
                "Invalid command. Usage: start-game <front|back>"
              );
              break;
            }

            client.lobbyJoinGame(
              command[1].equals("front")
                ? CardSideType.FRONT
                : CardSideType.BACK
            );
            break;
          case "show":
            switch (command[1]) {
              case "playerboard":
                final String nickname = command[2];
                cli.drawPlayerBoard(
                  game
                    .getPlayers()
                    .stream()
                    .filter(player -> player.getNickname().equals(nickname))
                    .findFirst()
                    .orElse(game.getPlayer())
                );
                break;
              case "leaderboard":
                cli.drawLeaderBoard(game.getPlayers());
                break;
              case "card":
                Position pos;

                try {
                  pos = new Position(
                    Integer.parseInt(command[2]),
                    Integer.parseInt(command[3])
                  );
                } catch (NumberFormatException e) {
                  cli.postNotification(
                    NotificationType.ERROR,
                    "Invalid command. Usage: show card <row> <column>"
                  );
                  break;
                }

                Pair<Card, CardSideType> entry = game
                  .getPlayer()
                  .getPlayedCards()
                  .get(pos);

                if (entry != null) cli.drawCard(entry.getKey());
                else cli.postNotification(
                  NotificationType.ERROR,
                  "No card at position " + pos
                );
                break;
              case "hand":
                cli.drawHand(game.getPlayer().getHand());
                break;
              case "objective":
                cli.drawCard(game.getSecretObjective());
                break;
              case "pairs":
                cli.drawPairs(game.getResourceCards(), game.getGoldCards());
                break;
              default:
                cli.postNotification(
                  NotificationType.ERROR,
                  "Invalid command. Usage: show <playerboard|leaderboard|card|hand|objective|pairs>"
                );
            }
            break;
          case "place":
            if (
              !Stream.of(
                command[1],
                command[2],
                command[3],
                command[4]
              ).allMatch(Objects::nonNull) ||
              !Stream.of(command[1], command[2], command[3]).allMatch(
                s -> s.matches("\\d+")
              ) ||
              !List.of("front", "back").contains(command[4])
            ) {
              cli.postNotification(
                NotificationType.ERROR,
                "Invalid command. Usage: place <hand number> <row> <column> <front|back>"
              );
              break;
            }

            Integer handIndex;
            Position position;
            try {
              handIndex = Integer.parseInt(command[1]);
              position = new Position(
                Integer.parseInt(command[2]),
                Integer.parseInt(command[3])
              );
            } catch (NumberFormatException e) {
              cli.postNotification(
                NotificationType.ERROR,
                "Invalid command. Usage: place <hand number> <row> <column> <front|back>"
              );
              break;
            }

            client.placeCard(
              handIndex,
              CardSideType.valueOf(command[4].toUpperCase()),
              position
            );
            break;
          case "draw":
            if (
              command[1] != null &&
              !List.of(
                "deck",
                "resource1",
                "resource2",
                "gold1",
                "gold2"
              ).contains(command[1])
            ) {
              cli.postNotification(
                NotificationType.ERROR,
                "Invalid command. Usage: draw [deck|resource1|resource2|gold1|gold2]"
              );
              break;
            }

            if (command[1] != null) {
              DrawingCardSource source = command[1].equals("deck")
                ? DrawingCardSource.Deck
                : (command[1].endsWith("1")
                    ? DrawingCardSource.CardPairFirstCard
                    : DrawingCardSource.CardPairSecondCard);

              DrawingDeckType type = command[1].startsWith("resource")
                ? DrawingDeckType.RESOURCE
                : DrawingDeckType.GOLD;

              client.nextTurn(source, type);
            } else client.nextTurn();
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
