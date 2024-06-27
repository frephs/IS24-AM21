package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class Cli implements View {

  private final LocalModelContainer localModel;

  public static class Options {

    private static Boolean colored;

    public Options(Boolean colored) {
      Options.colored = colored;
      //TODO remove color from prompt since it's still colored, at least outside of the jar
    }

    public static Boolean isColored() {
      return colored;
    }
  }

  static Cli.Options options;

  @Override
  public void setClient(ClientConnectionHandler client) {
    // Not used in CLI
  }

  public Cli() {
    localModel = new LocalModelContainer();
  }

  @Override
  public LocalModelContainer getLocalModel() {
    return localModel;
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String message
  ) {
    printUpdate(
      CliUtils.colorize(
        message,
        notificationType.getColor(),
        notificationType == NotificationType.ERROR
          ? ColorStyle.BOLD
          : ColorStyle.NORMAL
      )
    );
  }

  @Override
  public void postNotification(Notification notification) {
    postNotification(
      notification.getNotificationType(),
      notification.getMessage()
    );
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String[] parts,
    Colorable colorable,
    int colorableIndex
  ) {
    ArrayList<String> result = new ArrayList<>();
    Arrays.stream(parts)
      .limit(2)
      .map(
        e ->
          CliUtils.colorize(e, notificationType.getColor(), ColorStyle.NORMAL)
      )
      .forEach(result::add);
    result.add(
      CliUtils.colorize(
        colorable,
        colorableIndex == 0 ? ColorStyle.BOLD : ColorStyle.NORMAL
      )
    );
    Arrays.stream(parts)
      .skip(2)
      .map(
        e ->
          CliUtils.colorize(e, notificationType.getColor(), ColorStyle.NORMAL)
      )
      .forEach(result::add);
    printUpdate(String.join("", result));
  }

  public void printPrompt() {
    System.out.print("\r> ");
  }

  public void printUpdate(String string) {
    System.out.println(
      "\r" +
      string +
      " ".repeat(string.length() <= 100 ? 100 - string.length() : 0)
    );
    printPrompt();
  }

  @Override
  public void listGames() {
    postNotification(
      NotificationType.CONFIRM,
      "Successfully sent to the server"
    );
    drawAvailableGames();
  }

  void diffMessage(int diff, String attributeName) {
    if (diff != 0) {
      postNotification(
        NotificationType.UPDATE,
        getLocalModel()
          .getLocalGameBoard()
          .orElseThrow()
          .getCurrentPlayer()
          .getNickname() +
        (diff > 0 ? "gained" : "lost" + diff) +
        attributeName +
        ((Math.abs(diff) != 1) ? "s" : "") +
        ". "
      );
    }
  }

  void diffMessage(int diff, Colorable colorable) {
    if (diff != 0) {
      postNotification(
        NotificationType.UPDATE,
        new String[] {
          getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getCurrentPlayer()
            .getNickname() +
          (diff > 0 ? " gained " : " lost " + diff),
          ((Math.abs(diff) != 1) ? "s" : ""),
          ". ",
        },
        colorable,
        2
      );
    }
  }

  public void displayException(Exception e) {
    printUpdate(
      CliUtils.colorize(
        e.getMessage() +
        "\n " +
        Stream.of(e.getStackTrace())
          .map(StackTraceElement::toString)
          .collect(Collectors.joining("\n")),
        Color.RED,
        ColorStyle.UNDERLINED
      )
    );
  }

  @Override
  public void drawAvailableGames() {
    List<GameEntry> gameEntries = getLocalModel()
      .getLocalMenu()
      .getGames()
      .values()
      .stream()
      .toList();

    if (!gameEntries.isEmpty()) {
      printUpdate(
        CliUtils.getTable(
          new String[] {
            "GameId",
            "Current number of players",
            "Max number of players",
          },
          new ArrayList<>(
            gameEntries.stream().map(GameEntry::getGameId).toList()
          ),
          new ArrayList<>(
            gameEntries
              .stream()
              .map(gameEntry -> String.valueOf(gameEntry.getCurrentPlayers()))
              .toList()
          ),
          new ArrayList<>(
            gameEntries
              .stream()
              .map(gameEntry -> String.valueOf(gameEntry.getMaxPlayers()))
              .toList()
          )
        )
      );
    } else {
      printUpdate(
        "No games available, create one with `create-game <name> <players>`"
      );
    }
  }

  @Override
  public void drawAvailableTokenColors() {
    printUpdate(
      "The available token colors are: " +
      localModel
        .getLocalLobby()
        .orElseThrow()
        .getAvailableTokens()
        .stream()
        .map(token -> CliUtils.colorize(token, ColorStyle.NORMAL))
        .collect(Collectors.joining(" "))
    );
  }

  @Override
  public void drawLobby() {
    printUpdate(
      "Lobby: " +
      localModel
        .getLocalLobby()
        .orElseThrow()
        .getPlayers()
        .values()
        .stream()
        .map(localPlayer -> {
          String nickname = localPlayer.getNickname();
          TokenColor token = localPlayer.getToken();
          return (
            CliUtils.colorize(
              (nickname != null) ? nickname : "<pending>",
              token != null ? token.getColor() : Color.GRAY,
              ColorStyle.NORMAL
            )
          );
        })
        .collect(Collectors.joining(" "))
    );
  }

  /** Don't use this in CLI, just draw the individual components when needed */
  @Override
  public void drawGameBoard() {}

  @Override
  public void drawLeaderBoard() {
    printUpdate(
      "Leaderboard:\n" +
      localModel
        .getLocalLobby()
        .orElseThrow()
        .getPlayers()
        .values()
        .stream()
        .map(player -> {
          String nickname = player.getNickname();
          ColorStyle nicknameStyle = ColorStyle.NORMAL;

          if (
            nickname.equals(
              localModel
                .getLocalGameBoard()
                .orElseThrow()
                .getCurrentPlayer()
                .getNickname()
            )
          ) {
            nickname = "> " + nickname;
            nicknameStyle = ColorStyle.BOLD;
          }

          int points = player.getPoints();
          return (
            CliUtils.colorize(
              nickname,
              player.getToken().getColor(),
              nicknameStyle
            ) +
            " - " +
            CliUtils.colorize(
              String.valueOf(points),
              Color.YELLOW,
              ColorStyle.BOLD
            )
          );
        })
        .collect(Collectors.joining("\n"))
    );
  }

  @Override
  public void drawPlayerBoards() {
    localModel
      .getLocalLobby()
      .orElseThrow()
      .getPlayers()
      .values()
      .forEach(player -> {
        printUpdate(
          "Player " +
          CliUtils.colorize(
            player.getNickname(),
            player.getToken().getColor(),
            ColorStyle.NORMAL
          ) +
          ":"
        );
        drawPlayerBoard(player.getNickname());
      });
  }

  @Override
  public void drawPlayerBoard(
    String nickname,
    int verticalOffset,
    int horizontalOffset
  ) {
    LocalPlayer player = localModel
      .getLocalGameBoard()
      .orElseThrow()
      .getPlayerByNickname(nickname)
      .orElseThrow();

    CliPlayerBoard.drawPlayerBoard(
      player.getPlayedCardsByOrder(),
      player.getAvailableSpots().orElseThrow(),
      verticalOffset,
      horizontalOffset
    );
    //    player
    //      .getPlayedCardsByPosition()
    //      .forEach(
    //        (position, placedCard) ->
    //          printUpdate(
    //            "Card " +
    //            (placedCard.getKey().getId()) +
    //            " placed at " +
    //            position +
    //            " on side " +
    //            placedCard.getValue()
    //          )
    //      );
    //    printUpdate("Available spots: ");
    //    player
    //      .getAvailableSpots()
    //      .ifPresent(
    //        availableSpots ->
    //          availableSpots.forEach(position -> printUpdate(position.toString()))
    //      );
    //    printUpdate("Forbidden spots: ");
    //    player
    //      .getForbiddenSpots()
    //      .ifPresent(
    //        forbiddenSpots ->
    //          forbiddenSpots.forEach(position -> printUpdate(position.toString()))
    //      );
  }

  @Override
  public void drawGame() {
    printUpdate(
      "Game started with " +
      localModel.getLocalGameBoard().orElseThrow().getPlayers().size() +
      " players"
    );
  }

  @Override
  public void drawGameOver() {
    printUpdate("Game over");
    winningPlayer(
      localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getPlayers()
        .stream()
        .min((p1, p2) -> p2.getPoints() - p1.getPoints())
        .map(LocalPlayer::getNickname)
        .orElseThrow()
    );
  }

  @Override
  public void drawCard(Card card) {
    printUpdate("Card drawn:\n" + card.cardToAscii());
  }

  @Override
  public void drawHand() {
    AtomicInteger index = new AtomicInteger();

    printUpdate(
      "Hand:\n" +
      localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getPlayer()
        .getHand()
        .stream()
        .map(Card::cardToAscii)
        .map(card -> {
          index.getAndIncrement();
          return index + ": \n" + card;
        })
        .collect(Collectors.joining("\n"))
    );
  }

  @Override
  public void drawPairs() {
    CardPair<Card> resourceCards = localModel
      .getLocalGameBoard()
      .orElseThrow()
      .getResourceCards();
    CardPair<Card> goldCards = localModel
      .getLocalGameBoard()
      .orElseThrow()
      .getGoldCards();

    printUpdate(
      "Resource cards pair:\n" +
      resourceCards.getFirst().cardToAscii() +
      "\n" +
      resourceCards.getSecond().cardToAscii() +
      "\n" +
      "Gold cards pair:\n" +
      goldCards.getFirst().cardToAscii() +
      "\n" +
      goldCards.getSecond().cardToAscii()
    );
  }

  @Override
  public void drawObjectiveCardChoice() {
    CardPair<Card> cardPair = localModel
      .getLocalLobby()
      .orElseThrow()
      .getAvailableObjectives();

    printUpdate(
      "Objective cards pair:\n" +
      cardPair.getFirst().cardToAscii() +
      "\n" +
      cardPair.getSecond().cardToAscii()
    );
  }

  @Override
  public void drawStarterCardSides() {
    printUpdate(
      "Starter card sides:\n" +
      localModel.getLocalLobby().orElseThrow().getStarterCard().cardToAscii()
    );
  }

  @Override
  public void drawChatMessage(ChatMessage message) {
    printUpdate(
      CliUtils.colorize(
        message.getSender(),
        localModel
          .getLocalGameBoard()
          .get()
          .getPlayers()
          .stream()
          .filter(player -> player.getNickname().equals(message.getSender()))
          .findFirst()
          .orElseThrow()
          .getToken()
          .getColor(),
        ColorStyle.BOLD
      ) +
      message.getRecipient().map(recipient -> " whispered ").orElse(" said ") +
      message
        .getRecipient()
        .map(
          recipient ->
            CliUtils.colorize(
              message.getContent(),
              Color.PURPLE,
              ColorStyle.NORMAL
            )
        )
        .orElse(message.getContent())
    );
  }

  @Override
  public void drawCommonObjectiveCards() {
    CardPair<Card> cardPair = localModel
      .getLocalGameBoard()
      .orElseThrow()
      .getObjectiveCards();

    printUpdate(
      "Common objective cardPair:\n" +
      cardPair.getFirst().cardToAscii() +
      "\n" +
      cardPair.getSecond().cardToAscii()
    );
  }

  @Override
  public void drawPlayerObjective() {
    printUpdate(
      "Player objective card:\n" +
      localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getPlayer()
        .getObjectiveCard()
        .cardToAscii()
    );
  }

  @Override
  public void drawCardDecks() {
    PlayableCard firstResourceCard = localModel
      .getLocalGameBoard()
      .orElseThrow()
      .getResourceDeckTopCard();
    PlayableCard firstGoldCard = localModel
      .getLocalGameBoard()
      .orElseThrow()
      .getGoldDeckTopCard();

    if (firstResourceCard != null) {
      printUpdate(
        "Resource cards deck:\n" +
        firstResourceCard.getSides().get(1).cardToAscii()
      );
    } else {
      printUpdate("Resource cards deck:\n" + "Empty deck");
    }
    if (firstGoldCard != null) {
      printUpdate(
        "Gold cards deck:\n" + firstGoldCard.getSides().get(1).cardToAscii()
      );
    } else {
      printUpdate("Gold cards deck:\n" + "Empty deck");
    }
  }

  @Override
  public void lobbyInfo(LobbyUsersInfo usersInfo) {
    drawAvailableTokenColors();
    drawLobby();
  }

  @Override
  public void getObjectiveCards(Pair<Integer, Integer> objectiveCards) {
    drawObjectiveCardChoice();
  }

  @Override
  public void getStarterCard(Integer cardId) {
    drawStarterCardSides();
  }

  @Override
  public void drawNicknameChoice() {}

  @Override
  public void cardPlaced(
    String gameId,
    String playerId,
    Integer playerHandCardNumber,
    Integer cardId,
    CardSideType side,
    Position position,
    int newPlayerScore,
    Map<ResourceType, Integer> updatedResources,
    Map<ObjectType, Integer> updatedObjects,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
  ) {
    View.super.cardPlaced(
      gameId,
      playerId,
      playerHandCardNumber,
      cardId,
      side,
      position,
      newPlayerScore,
      updatedResources,
      updatedObjects,
      availableSpots,
      forbiddenSpots
    );

    diffMessage(
      newPlayerScore -
      getLocalModel()
        .getLocalGameBoard()
        .orElseThrow()
        .getCurrentPlayer()
        .getPoints(),
      "point"
    );

    Arrays.stream(ResourceType.values()).forEach(
      resourceType ->
        diffMessage(
          updatedResources.get(resourceType) -
          getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getCurrentPlayer()
            .getResources()
            .get(resourceType),
          resourceType
        )
    );

    Arrays.stream(ObjectType.values()).forEach(
      objectType ->
        diffMessage(
          updatedObjects.get(objectType) -
          getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getCurrentPlayer()
            .getObjects()
            .get(objectType),
          objectType
        )
    );
  }

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores) {
    View.super.playerScoresUpdate(newScores);
    newScores.forEach((nickname, newScore) ->
      getLocalModel()
        .getLocalGameBoard()
        .orElseThrow()
        .getPlayers()
        .stream()
        .filter(player -> player.getNickname().equals(nickname))
        .forEach(player -> {
          int diff = newScore - player.getPoints();
          player.setPoints(newScore);
          diffMessage(diff, "points");
        }));
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID connectionID) {
    View.super.playerJoinedLobby(gameId, connectionID);
  }
}
