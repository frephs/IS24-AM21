package polimi.ingsw.am21.codex;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
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
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class DummyView implements View {

  final String id;

  Cli.Options options;

  public DummyView(String id) {
    super();
    this.id = id;
    this.options = new Cli.Options(true);
  }

  @Override
  public LocalModelContainer getLocalModel() {
    return null;
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String message
  ) {
    System.out.println(
      CliUtils.colorize(
        "[" + id + "] " + message,
        notificationType.getColor(),
        ColorStyle.NORMAL
      )
    );
  }

  @Override
  public void postNotification(Notification notification) {}

  @Override
  public void postNotification(
    NotificationType notificationType,
    String[] messages,
    Colorable colorable,
    int colorableIndex
  ) {}

  @Override
  public void connected() {}

  @Override
  public void displayException(Exception e) {
    CliUtils.colorize(
      "[" + id + "] " + e.getMessage(),
      NotificationType.ERROR.getColor(),
      ColorStyle.NORMAL
    );

    e.printStackTrace();
  }

  @Override
  public void drawAvailableGames(List<GameEntry> games) {}

  @Override
  public void drawAvailableTokenColors(Set<TokenColor> tokenColors) {}

  @Override
  public void drawLobby(Map<UUID, LocalPlayer> players) {}

  @Override
  public void drawLeaderBoard(List<LocalPlayer> players) {}

  @Override
  public void drawPlayerBoards(List<LocalPlayer> players) {}

  @Override
  public void drawPlayerBoard(LocalPlayer player) {}

  @Override
  public void drawCardDrawn(DrawingDeckType deck, Card card) {}

  @Override
  public void drawCardDrawn(DrawingDeckType deck) {}

  @Override
  public void drawCardPlacement(
    Card card,
    CardSideType side,
    Position position,
    Set<Position> availablePositions,
    Set<Position> forbiddenPositions
  ) {}

  @Override
  public void drawGame(List<LocalPlayer> players) {}

  @Override
  public void drawGameOver(List<LocalPlayer> players) {}

  @Override
  public void drawCard(Card card) {}

  @Override
  public void drawHand(List<Card> hand) {}

  @Override
  public void drawPairs(
    CardPair<Card> resourceCards,
    CardPair<Card> goldCards
  ) {}

  @Override
  public void drawObjectiveCardChoice(CardPair<Card> cardPair) {}

  @Override
  public void drawStarterCardSides(Card cardId) {}

  @Override
  public void drawChatMessage(ChatMessage message) {
    System.out.println(
      CliUtils.colorize(
        "[" +
        id +
        "] received a message from: " +
        message.getSender() +
        ": " +
        message.getContent(),
        NotificationType.UPDATE.getColor(),
        ColorStyle.NORMAL
      )
    );
  }

  @Override
  public void drawCommonObjectiveCards(CardPair<Card> cardPair) {}

  @Override
  public void drawPlayerObjective(Card card) {}

  @Override
  public void drawCardDecks(
    PlayableCard firstResourceCard,
    PlayableCard firstGoldCard
  ) {}

  @Override
  public void drawNicknameChoice() {}
}
