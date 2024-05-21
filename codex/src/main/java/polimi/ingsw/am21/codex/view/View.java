package polimi.ingsw.am21.codex.view;

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
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public interface View {
  void postNotification(NotificationType notificationType, String message);

  void postNotification(Notification notification);

  void postNotification(
    NotificationType notificationType,
    String[] messages,
    Colorable colorable,
    int colorableIndex
  );

  // lobby
  void drawAvailableGames(
    Set<String> gameIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  );

  void drawAvailableTokenColors(Set<TokenColor> tokenColors);

  void drawLobby(
    Map<UUID, TokenColor> availableTokens,
    Map<UUID, String> playerNicknames
  );

  // game

  void drawLeaderBoard(List<LocalPlayer> players);

  void drawPlayerBoards(List<LocalPlayer> players);

  void drawPlayerBoard(LocalPlayer player);

  /**
   * Displays that the client player has drawn a card from a deck
   * @param deck The deck the card has been drawn from
   * @param card The card that has been drawn
   */
  void drawCardDrawn(DrawingDeckType deck, Card card);

  /**
   * Displays that a card has been drawn from a deck
   * @param deck The deck that the card has been drawn from
   */
  void drawCardDrawn(DrawingDeckType deck);

  void drawLeaderBoard();

  void drawCardPlacement(Card card, CardSideType side, Position position);

  void drawGame(List<LocalPlayer> players);

  void drawGameOver(List<LocalPlayer> players);

  void drawCard(Card card);

  /**
   * Displays the hand of the client player
   */
  void drawHand(List<Card> hand);

  /**
   * Displays the pairs the players can draw from
   * @param resourceCards The resource cards pair
   * @param goldCards The gold cards pair
   */

  void drawPairs(CardPair<Card> resourceCards, CardPair<Card> goldCards);

  void drawObjectiveCardChoice(CardPair<Card> cardPair);
  void drawStarterCardSides(Card cardId);
  void drawWinner(String nickname);
}
