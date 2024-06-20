package polimi.ingsw.am21.codex.client.localModel.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
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
  public void refreshLobbies(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) throws RemoteException {
    listener.refreshLobbies(lobbyIds, currentPlayers, maxPlayers);
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
  public void playerSetToken(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor token
  ) throws RemoteException {
    listener.playerSetToken(gameId, socketID, nickname, token);
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
    String nickname
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
  public void gameStarted(String gameId, GameInfo gameInfo)
    throws RemoteException {
    listener.gameStarted(gameId, gameInfo);
  }

  @Override
  public void changeTurn(
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
  ) throws RemoteException {
    listener.changeTurn(
      gameId,
      playerNickname,
      playerIndex,
      isLastRound,
      source,
      deck,
      cardId,
      newPairCardId,
      availableSpots,
      forbiddenSpots,
      resourceDeckTopCardId,
      goldDeckTopCardId
    );
  }

  @Override
  public void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) throws RemoteException {
    listener.changeTurn(
      gameId,
      playerNickname,
      playerIndex,
      isLastRound,
      availableSpots,
      forbiddenSpots,
      resourceDeckTopCardId,
      goldDeckTopCardId
    );
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
  public void remainingRounds(String gameID, int remainingRounds)
    throws RemoteException {
    listener.remainingRounds(gameID, remainingRounds);
  }

  @Override
  public void winningPlayer(String nickname) throws RemoteException {
    listener.winningPlayer(nickname);
  }

  @Override
  public void playerConnectionChanged(
    UUID socketID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) throws RemoteException {
    listener.playerConnectionChanged(socketID, nickname, status);
  }

  @Override
  public void lobbyInfo(LobbyUsersInfo usersInfo) throws RemoteException {
    listener.lobbyInfo(usersInfo);
  }

  @Override
  public void chatMessage(String gameID, ChatMessage message)
    throws RemoteException {
    listener.chatMessage(gameID, message);
  }
}
