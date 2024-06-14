package polimi.ingsw.am21.codex.controller.listeners;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface RemoteGameEventListener extends Remote {
  void gameCreated(String gameId, int currentPlayers, int maxPlayers)
    throws RemoteException;

  void gameDeleted(String gameId) throws RemoteException;

  void playerJoinedLobby(String gameId, UUID socketID) throws RemoteException;

  void playerLeftLobby(String gameId, UUID socketID) throws RemoteException;

  void playerSetToken(String gameId, UUID socketID, TokenColor token)
    throws RemoteException;

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

  void gameStarted(
    String gameId,
    List<String> players,
    Pair<Integer, Integer> goldCardPairIds,
    Pair<Integer, Integer> resourceCardPairIds,
    Pair<Integer, Integer> commonObjectivesIds
  ) throws RemoteException;

  /**
   * @param playerId The player that has just finished their turn
   */
  void changeTurn(
    String gameId,
    String playerId,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Integer newPairCardId
  ) throws RemoteException;

  /**
   * @param playerId The player that has just finished their turn
   */
  void changeTurn(String gameId, String playerId, Boolean isLastRound)
    throws RemoteException;

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

  void remainingTurns(int remainingTurns) throws RemoteException;

  void winningPlayer(String nickname) throws RemoteException;

  void chatMessageSent(String gameId, ChatMessage message)
    throws RemoteException;
}
