package polimi.ingsw.am21.codex.client.localModel.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LocalModelGameEventListener
  extends UnicastRemoteObject
  implements RemoteGameEventListener {

  private final GameEventListener listener;

  public LocalModelGameEventListener(GameEventListener listener)
    throws RemoteException {
    this.listener = listener;
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers)
    throws RemoteException {
    listener.gameCreated(gameId, currentPlayers, maxPlayers);
  }

  @Override
  public void gameDeleted(String gameId) throws RemoteException {
    listener.gameDeleted(gameId);
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID socketID)
    throws RemoteException {
    listener.playerJoinedLobby(gameId, socketID);
  }

  @Override
  public void playerLeftLobby(String gameId, UUID socketID)
    throws RemoteException {
    listener.playerLeftLobby(gameId, socketID);
  }

  @Override
  public void playerSetToken(String gameId, UUID socketID, TokenColor token)
    throws RemoteException {
    listener.playerSetToken(gameId, socketID, token);
  }

  @Override
  public void playerSetNickname(String gameId, UUID socketID, String nickname)
    throws RemoteException {
    listener.playerSetNickname(gameId, socketID, nickname);
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID socketID,
    Optional<String> nickname
  ) throws RemoteException {
    listener.playerChoseObjectiveCard(gameId, socketID, nickname);
  }

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardId,
    CardSideType starterSide
  ) throws RemoteException {
    listener.playerJoinedGame(
      gameId,
      socketID,
      nickname,
      color,
      handIDs,
      starterCardId,
      starterSide
    );
  }

  @Override
  public void gameStarted(String gameId, List<String> players)
    throws RemoteException {
    listener.gameStarted(gameId, players);
  }

  @Override
  public void changeTurn(
    String gameId,
    String playerId,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Integer newPairCardId
  ) throws RemoteException {
    listener.changeTurn(
      gameId,
      playerId,
      isLastRound,
      source,
      deck,
      cardId,
      newPairCardId
    );
  }

  @Override
  public void changeTurn(String gameId, String playerId, Boolean isLastRound)
    throws RemoteException {
    listener.changeTurn(gameId, playerId, isLastRound);
  }

  @Override
  public void cardPlaced(
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
  ) throws RemoteException {
    listener.cardPlaced(
      gameId,
      playerId,
      playerHandCardNumber,
      cardId,
      side,
      position,
      newPlayerScore,
      updatedResources,
      updatedObjects,
      availableSpots,
      forbiddenSpots
    );
  }

  @Override
  public void gameOver() throws RemoteException {
    listener.gameOver();
  }

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores)
    throws RemoteException {
    listener.playerScoresUpdate(newScores);
  }

  @Override
  public void remainingTurns(int remainingTurns) throws RemoteException {
    listener.remainingTurns(remainingTurns);
  }

  @Override
  public void winningPlayer(String nickname) throws RemoteException {
    listener.winningPlayer(nickname);
  }
}
