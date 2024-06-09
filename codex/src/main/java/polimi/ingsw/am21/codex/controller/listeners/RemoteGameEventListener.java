package polimi.ingsw.am21.codex.controller.listeners;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface RemoteGameEventListener extends Remote {
  void gameCreated(String gameId, int currentPlayers, int maxPlayers)
    throws RemoteException;

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
   * @param playerId The player that has just finished their turn
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
    Set<Position> forbiddenSpots
  ) throws RemoteException;

  /**
   * @param playerId The player that has just finished their turn
   */
  void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
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
}
