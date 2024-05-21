package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
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
    // TODO
  }

  @Override
  public void postNotification(Notification notification) {
    // TODO
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String[] messages,
    Colorable colorable,
    int colorableIndex
  ) {
    // TODO
  }

  @Override
  public void drawAvailableGames(
    Set<String> gameIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {
    // TODO
  }

  @Override
  public void drawAvailableTokenColors(Set<TokenColor> tokenColors) {
    // TODO
  }

  @Override
  public void drawLobby(
    Map<UUID, TokenColor> availableTokens,
    Map<UUID, String> playerNicknames
  ) {
    // TODO

  }

  @Override
  public void drawLeaderBoard(List<LocalPlayer> players) {
    // TODO
  }

  @Override
  public void drawPlayerBoards(List<LocalPlayer> players) {
    // TODO
  }

  @Override
  public void drawPlayerBoard(LocalPlayer player) {
    // TODO
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck, Card card) {
    // TODO
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck) {
    // TODO
  }

  @Override
  public void drawLeaderBoard() {
    // TODO
  }

  @Override
  public void drawCardPlacement(
    Card card,
    CardSideType side,
    Position position
  ) {
    // TODO
  }

  @Override
  public void drawGame(List<LocalPlayer> players) {
    // TODO
  }

  @Override
  public void drawGameOver(List<LocalPlayer> players) {
    // TODO
  }

  @Override
  public void drawCard(Card card) {
    // TODO
  }

  @Override
  public void drawHand(List<Card> hand) {
    // TODO
  }

  @Override
  public void drawPairs(
    CardPair<Card> resourceCards,
    CardPair<Card> goldCards
  ) {
    // TODO
  }

  @Override
  public void drawWinner(String nickname) {
    // TODO
  }
}
