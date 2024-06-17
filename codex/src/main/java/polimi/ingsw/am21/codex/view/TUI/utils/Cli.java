package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class Cli implements View {

  public static class Options {

    private final Boolean colored;

    public Options(Boolean colored) {
      this.colored = colored;
    }

    public Boolean isColored() {
      return colored;
    }
  }

  Cli.Options options;

  public Cli(Cli.Options options) {
    this.options = options;
  }

  public Boolean isColored() {
    return options.isColored();
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

  public void displayException(Exception e) {
    printUpdate(
      CliUtils.colorize(
        options,
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
  public void postNotification(
    NotificationType notificationType,
    String message
  ) {
    printUpdate(
      CliUtils.colorize(
        options,
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
          CliUtils.colorize(
            options,
            e,
            notificationType.getColor(),
            ColorStyle.NORMAL
          )
      )
      .forEach(result::add);
    result.add(
      CliUtils.colorize(
        options,
        colorable,
        colorableIndex == 0 ? ColorStyle.BOLD : ColorStyle.NORMAL
      )
    );
    Arrays.stream(parts)
      .skip(2)
      .map(
        e ->
          CliUtils.colorize(
            options,
            e,
            notificationType.getColor(),
            ColorStyle.NORMAL
          )
      )
      .forEach(result::add);
    printUpdate(String.join("", result));
  }

  @Override
  public void drawAvailableGames(List<GameEntry> gameEntries) {
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
  }

  @Override
  public void drawAvailableTokenColors(Set<TokenColor> tokenColors) {
    printUpdate(
      "The available token colors are: " +
      tokenColors
        .stream()
        .map(token -> CliUtils.colorize(options, token, ColorStyle.NORMAL))
        .collect(Collectors.joining(" "))
    );
  }

  @Override
  public void drawLobby(Map<UUID, LocalPlayer> players) {
    printUpdate(
      "Lobby: " +
      players
        .values()
        .stream()
        .map(localPlayer -> {
          String nickname = localPlayer.getNickname();
          TokenColor token = localPlayer.getToken();
          return (
            CliUtils.colorize(
              options,
              (nickname != null) ? nickname : "<pending>",
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
              options,
              nickname,
              player.getToken().getColor(),
              ColorStyle.NORMAL
            ) +
            " - " +
            CliUtils.colorize(
              options,
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
          options,
          player.getNickname(),
          player.getToken().getColor(),
          ColorStyle.NORMAL
        ) +
        ":"
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
    printUpdate("Available spots: ");
    player
      .getAvailableSpots()
      .ifPresent(
        availableSpots ->
          availableSpots.forEach(position -> printUpdate(position.toString()))
      );
    printUpdate("Forbidden spots: ");
    player
      .getForbiddenSpots()
      .ifPresent(
        forbiddenSpots ->
          forbiddenSpots.forEach(position -> printUpdate(position.toString()))
      );
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck, Card card) {
    printUpdate(
      "Card drawn from deck " + deck + ":\n" + card.cardToAscii(options)
    );
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck) {
    printUpdate("Card drawn from deck " + deck);
  }

  @Override
  public void drawCardPlacement(
    Card card,
    CardSideType side,
    Position position,
    Set<Position> availablePositions,
    Set<Position> forbiddenPositions
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
    printUpdate("Card drawn:\n" + card.cardToAscii(options));
  }

  @Override
  public void drawHand(List<Card> hand) {
    printUpdate(
      "Hand:\n" +
      hand
        .stream()
        .map(card -> card.cardToAscii(options))
        .collect(Collectors.joining("\n"))
    );
  }

  @Override
  public void drawPairs(
    CardPair<Card> resourceCards,
    CardPair<Card> goldCards
  ) {
    printUpdate(
      "Resource cards pair:\n" +
      resourceCards.getFirst().cardToAscii(options) +
      "\n" +
      resourceCards.getSecond().cardToAscii(options) +
      "\n" +
      "Gold cards pair:\n" +
      goldCards.getFirst().cardToAscii(options) +
      "\n" +
      goldCards.getSecond().cardToAscii(options)
    );
  }

  @Override
  public void drawObjectiveCardChoice(CardPair<Card> cardPair) {
    printUpdate(
      "Objective cards pair:\n" +
      cardPair.getFirst().cardToAscii(options) +
      "\n" +
      cardPair.getSecond().cardToAscii(options)
    );
  }

  @Override
  public void drawStarterCardSides(Card cardId) {
    printUpdate("Starter card sides:\n" + cardId.cardToAscii(options));
  }

  @Override
  public void drawWinner(String nickname) {
    printUpdate(
      CliUtils.colorize(
        options,
        "Winner: " + nickname,
        Color.GREEN,
        ColorStyle.BOLD
      )
    );
  }

  @Override
  public void drawChatMessage(ChatMessage message) {
    printUpdate(
      CliUtils.colorize(
        options,
        message.getSender(),
        Color.PURPLE,
        ColorStyle.BACKGROUND
      ) +
      "said " +
      message.getContent()
    );
  }

  @Override
  public void drawCommonObjectiveCards(CardPair<Card> cardPair) {
    printUpdate(
      "Common objective cardPair:\n" +
      cardPair.getFirst().cardToAscii(options) +
      "\n" +
      cardPair.getSecond().cardToAscii(options)
    );
  }

  @Override
  public void drawPlayerObjective(Card card) {
    // TODO
  }

  @Override
  public void drawCardDecks(
    PlayableCard firstResourceCard,
    PlayableCard firstGoldCard
  ) {
    if (firstResourceCard != null) {
      printUpdate(
        "Resource cards deck:\n" +
        firstResourceCard.getSides().get(1).cardToAscii(options)
      );
    } else {
      printUpdate("Resource cards deck:\n" + "Empty deck");
    }
    if (firstGoldCard != null) {
      printUpdate(
        "Gold cards deck:\n" +
        firstGoldCard.getSides().get(1).cardToAscii(options)
      );
    } else {
      printUpdate("Gold cards deck:\n" + "Empty deck");
    }
  }

  @Override
  public void drawNicknameChoice() {}
}
