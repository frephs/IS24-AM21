package polimi.ingsw.am21.codex.client;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.View;

public class ClientGameEventHandler implements GameEventListener {

  protected LocalModelContainer localModel;
  private View view;

  ClientGameEventHandler(View view, LocalModelContainer localModel) {
    this.localModel = localModel;
    this.view = view;
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    localModel.gameCreated(gameId, currentPlayers, maxPlayers);
    view.gameCreated(gameId, currentPlayers, maxPlayers);
  }

  @Override
  public void gameDeleted(String gameId) {
    localModel.gameDeleted(gameId);
    view.gameDeleted(gameId);
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID socketID) {
    localModel.playerJoinedLobby(gameId, socketID);
    view.playerJoinedLobby(gameId, socketID);
  }

  @Override
  public void playerLeftLobby(String gameId, UUID socketID) {
    localModel.playerLeftLobby(gameId, socketID);
    view.playerLeftLobby(gameId, socketID);
  }

  @Override
  public void playerSetToken(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor token
  ) {
    localModel.playerSetToken(gameId, socketID, nickname, token);
    view.playerSetToken(gameId, socketID, nickname, token);
  }

  @Override
  public void playerSetNickname(String gameId, UUID socketID, String nickname) {
    localModel.playerSetNickname(gameId, socketID, nickname);
    view.playerSetNickname(gameId, socketID, nickname);
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID socketID,
    String nickname
  ) {
    localModel.playerChoseObjectiveCard(gameId, socketID, nickname);
    view.playerChoseObjectiveCard(gameId, socketID, nickname);
  }

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardID,
    CardSideType starterSide
  ) {
    localModel.playerJoinedGame(
      gameId,
      socketID,
      nickname,
      color,
      handIDs,
      starterCardID,
      starterSide
    );
    view.playerJoinedGame(
      gameId,
      socketID,
      nickname,
      color,
      handIDs,
      starterCardID,
      starterSide
    );
  }

  @Override
  public void gameStarted(String gameId, GameInfo gameInfo) {
    localModel.gameStarted(gameId, gameInfo);
    view.gameStarted(gameId, gameInfo);
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
  ) {
    localModel.changeTurn(
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
    view.changeTurn(
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
  ) {
    localModel.changeTurn(
      gameId,
      playerNickname,
      playerIndex,
      isLastRound,
      availableSpots,
      forbiddenSpots,
      resourceDeckTopCardId,
      goldDeckTopCardId
    );
    view.changeTurn(
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
  ) {
    localModel.cardPlaced(
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
    view.cardPlaced(
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
  public void gameOver() {
    localModel.gameOver();
    view.gameOver();
  }

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores) {
    localModel.playerScoresUpdate(newScores);
    view.playerScoresUpdate(newScores);
  }

  @Override
  public void remainingRounds(String gameID, int remainingRounds) {
    localModel.remainingRounds(gameID, remainingRounds);
    view.remainingRounds(gameID, remainingRounds);
  }

  @Override
  public void winningPlayer(String nickname) {
    localModel.winningPlayer(nickname);
    view.winningPlayer(nickname);
  }

  @Override
  public void playerConnectionChanged(
    UUID socketID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    localModel.playerConnectionChanged(socketID, nickname, status);
    view.playerConnectionChanged(socketID, nickname, status);
  }

  @Override
  public void lobbyInfo(LobbyUsersInfo usersInfo) {
    localModel.lobbyInfo(usersInfo);
    view.lobbyInfo(usersInfo);
  }

  @Override
  public void chatMessage(String gameID, ChatMessage message) {
    localModel.chatMessage(gameID, message);
    view.chatMessage(gameID, message);
  }
}
