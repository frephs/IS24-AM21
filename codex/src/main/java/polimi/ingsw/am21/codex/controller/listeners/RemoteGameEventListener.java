package polimi.ingsw.am21.codex.controller.listeners;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface RemoteGameEventListener extends Remote {
  void gameCreated(String gameId, int currentPlayers, int maxPlayers)
    throws RemoteException;

  void refreshLobbies(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) throws RemoteException;

  void gameDeleted(String gameId) throws RemoteException;

  void playerJoinedLobby(String gameId, UUID socketID) throws RemoteException;

  void playerLeftLobby(String gameId, UUID socketID) throws RemoteException;

  void playerSetToken(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor token
  ) throws RemoteException;

  void playerSetNickname(String gameId, UUID socketID, String nickname)
    throws RemoteException;

  void playerChoseObjectiveCard(String gameId, UUID socketID, String nickname)
    throws RemoteException;

  void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCard,
    CardSideType starterSide
  ) throws RemoteException;

  void gameStarted(String gameId, GameInfo gameInfo) throws RemoteException;

  /**
   * This method is used to change the turn in the game.
   *
   * @param gameId The unique identifier of the game that has just ended.
   * @param playerNickname The nickname of the player that has just finished their turn. This can be null if the game ended because of a disconnection.
   * @param playerIndex The index of the player that has just finished their turn in the list of players.
   * @param isLastRound A boolean indicating if this is the last round of the game.
   * @param source The source from which the card was drawn.
   * @param deck The type of deck from which the card was drawn.
   * @param cardId The unique identifier of the card that was drawn. (null if the receiving client is not the one who drew the card)
   * @param newPairCardId The unique identifier of the new pair card that was drawn.
   * @param availableSpots A set of positions that are available for the next player to place a card.
   * @param forbiddenSpots A set of positions that are forbidden for the next player to place a card.
   * @throws RemoteException If a remote or network error occurs.
   */
  void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Integer newPairCardId,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) throws RemoteException;

  void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) throws RemoteException;

  /* current player placed a card */
  void cardPlaced(
    String gameId,
    String playerId,
    Integer playerHandCardNumber,
    Integer cardId,
    CardSideType side,
    Position position,
    int newPlayerScore,
    Map<ResourceType, Integer> updatedResources,
    Map<ObjectType, Integer> updatedObjects,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
  ) throws RemoteException;

  void gameOver() throws RemoteException;

  void playerScoresUpdate(Map<String, Integer> newScores)
    throws RemoteException;

  void remainingRounds(String gameID, int remainingRounds)
    throws RemoteException;

  void winningPlayer(String nickname) throws RemoteException;

  void playerConnectionChanged(
    UUID socketID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) throws RemoteException;

  void lobbyInfo(LobbyUsersInfo usersInfo) throws RemoteException;

  void chatMessage(String gameID, ChatMessage message) throws RemoteException;

  void getObjectiveCards(Pair<Integer, Integer> objectiveCards)
    throws RemoteException;
}
