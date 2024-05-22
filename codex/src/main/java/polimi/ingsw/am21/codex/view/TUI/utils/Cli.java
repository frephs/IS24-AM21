package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class Cli implements View {

  private static final Cli instance = new Cli(true);
  Boolean colored;

  private Cli(Boolean colored) {
    this.colored = colored;
  }

  public static Cli getInstance() {
    return instance;
  }

  public Boolean isColored() {
    return colored;
  }

  public void printPrompt() {
    System.out.print("\rEnter a command: ");
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
    postNotification(notification.notificationType, notification.message);
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

  @Override
  public void drawAvailableGames(
    Set<String> gameIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {
    gameIds.forEach(gameId -> {
      printUpdate(
        "Game ID: " +
        gameId +
        " - Players: " +
        currentPlayers.get(gameId) +
        "/" +
        maxPlayers.get(gameId)
      );
    });
  }

  @Override
  public void drawAvailableTokenColors(Set<TokenColor> tokenColors) {
    printUpdate(
      "Available token colors: " +
      tokenColors
        .stream()
        .map(token -> CliUtils.colorize(token, ColorStyle.NORMAL))
        .collect(Collectors.joining(" "))
    );
  }

  @Override
  public void drawLobby(
    Map<UUID, TokenColor> playerTokens,
    Map<UUID, String> playerNicknames,
    List<UUID> socketIds
  ) {
    printUpdate(
      "Lobby: " +
      socketIds
        .stream()
        .map(socketId -> {
          String nickname = playerNicknames.get(socketId);
          TokenColor token = playerTokens.get(socketId);
          return (
            CliUtils.colorize(
              nickname != null ? nickname : "<pending>",
              token != null ? token.getColor() : Color.GRAY,
              ColorStyle.NORMAL
            )
          );
        })
        .collect(Collectors.joining(" "))
    );
  }

  @Override
  public void drawLeaderBoard(List<LocalPlayer> players) {
    printUpdate(
      "Leaderboard:\n" +
      players
        .stream()
        .sorted((a, b) -> b.getPoints() - a.getPoints())
        .map(player -> {
          String nickname = player.getNickname();
          int points = player.getPoints();
          return (
            CliUtils.colorize(
              nickname,
              player.getToken().getColor(),
              ColorStyle.NORMAL
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
  public void drawPlayerBoards(List<LocalPlayer> players) {
    players.forEach(player -> {
      printUpdate(
        "Player " +
        CliUtils.colorize(
          player.getNickname(),
          player.getToken().getColor(),
          ColorStyle.NORMAL
        ) +
        ":\n"
      );
      drawPlayerBoard(player);
    });
  }

  @Override
  public void drawPlayerBoard(LocalPlayer player) {
    player
      .getPlayedCards()
      .forEach(
        (position, placedCard) ->
          printUpdate(
            "Card " +
            (placedCard.getKey().getId()) +
            " placed at " +
            position +
            " on side " +
            placedCard.getValue()
          )
      );
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck, Card card) {
    printUpdate("Card drawn from deck " + deck + ":\n" + card.cardToAscii());
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck) {
    printUpdate("Card drawn from deck " + deck);
  }

  @Override
  public void drawCardPlacement(
    Card card,
    CardSideType side,
    Position position
  ) {
    printUpdate(
      "Card " + card.getId() + " placed at " + position + " on side " + side
    );
  }

  @Override
  public void drawGame(List<LocalPlayer> players) {
    printUpdate("Game started with " + players.size() + " players");
  }

  @Override
  public void drawGameOver(List<LocalPlayer> players) {
    printUpdate("Game over");
  }

  @Override
  public void drawCard(Card card) {
    printUpdate("Card drawn:\n" + card.cardToAscii());
  }

  @Override
  public void drawHand(List<Card> hand) {
    printUpdate(
      "Hand:\n" +
      hand.stream().map(Card::cardToAscii).collect(Collectors.joining("\n"))
    );
  }

  @Override
  public void drawPairs(
    CardPair<Card> resourceCards,
    CardPair<Card> goldCards
  ) {
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
  public void drawObjectiveCardChoice(CardPair<Card> cardPair) {}

  @Override
  public void drawStarterCardSides(Card cardId) {}

  @Override
  public void drawWinner(String nickname) {
    printUpdate(
      CliUtils.colorize("Winner: " + nickname, Color.GREEN, ColorStyle.BOLD)
    );
  }
}
