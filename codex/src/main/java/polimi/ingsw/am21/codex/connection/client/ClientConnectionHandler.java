package polimi.ingsw.am21.codex.connection.client;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

public interface ClientConnectionHandler {
  void connect();

  /**
   * retrieves the list of available games
   */
  void listGames();

  /**
   * @param gameId the id of the game to connect to
   */
  void connectToGame(String gameId);

  /**
   * @param gameId the id of the game create and connect to
   */
  void createAndConnectToGame(String gameId, int numberPlayers);

  /**
   * checks if the game you are connected to has started
   */
  void checkIfGameStarted();

  /**
   * @param color the color of the chosen token color
   */
  void lobbySetToken(TokenColor color);

  /**
   * @return the set of the token that are already taken
   */
  Set<TokenColor> getTokens();

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

  /**
   * @return the state of the game
   */
  GameState getGameState();
}
