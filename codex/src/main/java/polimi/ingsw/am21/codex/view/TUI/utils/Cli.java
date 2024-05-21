package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.*;
import java.util.stream.Collectors;
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

  @Override
  public void postNotification(
    NotificationType notificationType,
    String message
  ) {
    System.out.println(
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
    String[] messages,
    Colorable colorable,
    int colorableIndex
  ) {
    Arrays.stream(messages).forEach(
      message -> postNotification(notificationType, message)
    );
  }

  @Override
  public void drawAvailableGames(
    Set<String> gameIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {
    gameIds.forEach(gameId -> {
      System.out.println(
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
    System.out.println(
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
    System.out.println(
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
    System.out.println(
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
      System.out.println(
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
          System.out.println(
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
    System.out.println(
      "Card drawn from deck " + deck + ":\n" + card.cardToAscii()
    );
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck) {
    System.out.println("Card drawn from deck " + deck);
  }

  @Override
  public void drawCardPlacement(
    Card card,
    CardSideType side,
    Position position
  ) {
    System.out.println(
      "Card " + card.getId() + " placed at " + position + " on side " + side
    );
  }

  @Override
  public void drawGame(List<LocalPlayer> players) {
    System.out.println("Game started with " + players.size() + " players");
  }

  @Override
  public void drawGameOver(List<LocalPlayer> players) {
    System.out.println("Game over");
  }

  @Override
  public void drawCard(Card card) {
    System.out.println("Card drawn:\n" + card.cardToAscii());
  }

  @Override
  public void drawHand(List<Card> hand) {
    System.out.println(
      "Hand:\n" +
      hand.stream().map(Card::cardToAscii).collect(Collectors.joining("\n"))
    );
  }

  @Override
  public void drawPairs(
    CardPair<Card> resourceCards,
    CardPair<Card> goldCards
  ) {
    System.out.println(
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
    System.out.println(
      CliUtils.colorize("Winner: " + nickname, Color.GREEN, ColorStyle.BOLD)
    );
  }
}
