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

  void disconnect();

  /**
   * retrieves the list of available games
   */
  void listGames() throws RemoteException;

  /**
   * @param gameId the id of the game to connect to
   */
  void connectToGame(String gameId)
    throws LobbyFullException, RemoteException, GameNotFoundException;

  /**
   * leaves the game the player has joined, if any
   * */
  void leaveGameLobby();

  /**
   * @param gameId the id of the game create and connect to
   */
  void createAndConnectToGame(String gameId, int numberPlayers)
    throws RemoteException, EmptyDeckException, GameAlreadyStartedException, LobbyFullException, GameNotFoundException;

  /**
   * checks if the game you are connected to has started
   */
  void checkIfGameStarted() throws RemoteException;

  /**
   * @param color the color of the chosen token color
   */
  void lobbySetToken(TokenColor color)
    throws GameAlreadyStartedException, GameNotFoundException;

  /**
   * @return the set of the token that are available
   */

  Set<TokenColor> getAvailableTokens();

  /**
   * @param nickname the nickname of the lobby player
   */
  void lobbySetNickname(String nickname)
    throws GameAlreadyStartedException, NicknameAlreadyTakenException, GameNotFoundException;

  /**
   * @param first true if the player selects the first card in the pair
   *              otherwise false
   */
  void lobbyChooseObjectiveCard(Boolean first)
    throws GameAlreadyStartedException, GameNotFoundException;

  /**
   * Sets the chosen starter card side and make player join game
   *
   * @param cardSide the starter card side chosen by the player
   */
  void lobbyJoinGame(CardSideType cardSide)
    throws GameNotReadyException, GameAlreadyStartedException, EmptyDeckException, IllegalCardSideChoiceException, IllegalPlacingPositionException, GameNotFoundException;

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
  )
    throws PlayerNotActive, IllegalCardSideChoiceException, RemoteException, IllegalPlacingPositionException, GameNotFoundException;

  /**
   *
   * draws a card from the game board and continues to the next turn
   *
   * @param drawingSource the source were we get the card
   * @param deckType the type of card that we draw
   */
  void nextTurn(DrawingCardSource drawingSource, DrawingDeckType deckType)
    throws PlayerNotActive, GameOverException, EmptyDeckException, InvalidNextTurnCallException, GameNotFoundException;

  /**
   * Goes to next turn ( called only when the game is in the last round, since you cannot draw a card in that case)
   */
  void nextTurn()
    throws PlayerNotActive, GameOverException, InvalidNextTurnCallException, RemoteException, GameNotFoundException;

  /**
   * @return the state of the game
   */
  GameState getGameState();
}
