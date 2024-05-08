package polimi.ingsw.am21.codex.connection.client;

import java.util.List;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface ClientConnectionHandler {
  void connect();

  /**
   * returns the list of available games
   *
   * @return the list of games
   */
  List<String> listGames();

  /**
   * @param gameId the id of the game to connect to
   * @return true if the connection was successful
   */
  boolean connectToGame(String gameId);

  /**
   * @param gameId the id of the game create and connect to
   * @return true if the connection was successful
   */
  boolean createAndConnectToGame(String gameId);

  /**
   * @return true if the game you are connected to has started ( false if it
   * is still in the lobby )
   */
  boolean checkIfGameStarted();

  /**
   * @param color the color of the chosen token color
   */
  void lobbySetToken(TokenColor color);

  /**
   * @param nickname the nickname of the lobby player
   */
  void lobbySetNickname(String nickname);

  /**
   * @param first true if the player selects the first card in the pair
   *              otherwise false
   */
  void lobbyChooseObjectiveCard(Boolean first);

  /**
   * Sets the chosen starter card side and make player join game
   *
   * @param cardSide the starter card side chosen by the player
   */
  void lobbyJoinGame(CardSideType cardSide);

  /**
   * Places a card in the grid
   *
   * @param playerHandCardNumber the index of the card to place from the player hand
   * @param side which side the player wants to place it
   * @param position the target position
   */
  void placeCard(
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  );

  /**
   *
   * draws a card from the game board and continues to the next turn
   *
   * @param drawingSource the source were we get the card
   * @param deckType the type of card that we draw
   */
  void nextTurn(DrawingCardSource drawingSource, DrawingDeckType deckType);

  /**
   * Goes to next turn ( called only when the game is in the last round, since you cannot draw a card in that case)
   */
  void nextTurn();
}
