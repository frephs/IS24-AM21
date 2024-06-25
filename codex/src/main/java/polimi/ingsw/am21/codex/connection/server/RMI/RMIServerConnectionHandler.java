package polimi.ingsw.am21.codex.connection.server.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface RMIServerConnectionHandler extends Remote {
  /**
   * Gets the list of available game IDs
   */
  Set<String> getGames() throws RemoteException;

  /**
   * Gets the current number of players in each game
   */
  Map<String, Integer> getGamesCurrentPlayers() throws RemoteException;

  /**
   * Gets the maximum number of players in each game
   */
  Map<String, Integer> getGamesMaxPlayers() throws RemoteException;

  /**
   * Gets the ids of the objective cards that the lobby has to offer to the player
   * @param connectionID The connection ID of the player
   */
  Pair<Integer, Integer> getLobbyObjectiveCards(UUID connectionID)
    throws RemoteException, InvalidActionException;

  /**
   * Gets the id of the starter card that the lobby has to offer to the player
   * @param connectionID The connection ID of the player
   */
  Integer getLobbyStarterCard(UUID connectionID)
    throws RemoteException, InvalidActionException;

  /**
   * Joins a lobby
   * @param connectionID The connection ID of the player
   * @param gameID The game ID of the lobby
   */
  void joinLobby(UUID connectionID, String gameID)
    throws RemoteException, InvalidActionException;

  /**
   * Sets the token color of the player
   * @param connectionID The connection ID of the player
   * @param color The token color of the player
   */
  void lobbySetTokenColor(UUID connectionID, TokenColor color)
    throws RemoteException, InvalidActionException;

  /**
   * Sets the nickname of the player
   * @param connectionID The connection ID of the player
   * @param nickname The nickname of the player
   */
  void lobbySetNickname(UUID connectionID, String nickname)
    throws RemoteException, InvalidActionException;

  /**
   * Chooses the objective card of the player
   * @param connectionID The connection ID of the player
   * @param first True if the player chooses the first objective card, false otherwise
   */
  void lobbyChooseObjective(UUID connectionID, Boolean first)
    throws RemoteException, InvalidActionException;

  /**
   * Joins a game, by selecting the side of the starter card
   * @param connectionID The connection ID of the player
   */
  void joinGame(UUID connectionID, String gameID, CardSideType sideType)
    throws RemoteException, InvalidActionException;

  /**
   * Creates a game
   * @param connectionID The connection ID of the player
   * @param gameId The game ID of the game
   * @param players The number of players of the game
   */
  void createGame(UUID connectionID, String gameId, Integer players)
    throws RemoteException, InvalidActionException;

  /**
   * Deletes a game
   * @param connectionID The connection ID of the player
   * @param gameId The game ID of the game
   */
  void deleteGame(UUID connectionID, String gameId)
    throws RemoteException, InvalidActionException;

  /**
   * Starts the next turn, to be used when a player is skipping their turn (for
   * example, when the player is disconnected)
   * @param connectionID The connection ID of the player
   */
  void nextTurn(UUID connectionID)
    throws RemoteException, InvalidActionException;

  /**
   * Starts the next turn by drawing cards from the deck
   * @param connectionID The connection ID of the player
   * @param drawingSource The source of the drawing cards
   * @param deckType The type of the drawing deck
   */
  void nextTurn(
    UUID connectionID,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) throws RemoteException, InvalidActionException;

  /**
   * Places a card on the game board
   * @param connectionID The connection ID of the player
   * @param playerHandCardNumber The index of the card in the player's hand
   * @param side The side of the card
   * @param position The position of the card
   */
  void placeCard(
    UUID connectionID,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  ) throws RemoteException, InvalidActionException;

  /**
   * Leaves a lobby
   * @param connectionID The connection ID of the player
   */
  void leaveLobby(UUID connectionID)
    throws RemoteException, InvalidActionException;

  /**
   * Gets the available tokens of the player
   * @param connectionID The connection ID of the player
   * @param message The message to be sent to the player
   */
  void sendChatMessage(UUID connectionID, ChatMessage message)
    throws RemoteException, InvalidActionException;

  /**
   * Connects the player to the server
   * @param connectionID The connection ID of the player
   * @param listener The listener of the player
   */
  void connect(UUID connectionID, RemoteGameEventListener listener)
    throws RemoteException;

  /**
   * Sends a heartbeat to the server
   * @param connectionID The connection ID of the player
   */
  void heartBeat(UUID connectionID) throws RemoteException;
}
