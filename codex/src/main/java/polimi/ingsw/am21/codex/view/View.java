package polimi.ingsw.am21.codex.view;

import java.util.*;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public abstract class View implements GameEventListener {

  private LocalModelContainer localModel;

  public View(LocalModelContainer localModel) {
    this.localModel = localModel;
  }

  public abstract void postNotification(
    NotificationType notificationType,
    String message
  );

  public abstract void postNotification(Notification notification);

  public abstract void postNotification(
    NotificationType notificationType,
    String[] messages,
    Colorable colorable,
    int colorableIndex
  );

  public LocalModelContainer getLocalModel() {
    return localModel;
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    localModel.gameCreated(gameId, currentPlayers, maxPlayers);
  }

  @Override
  public void gameDeleted(String gameId) {
    localModel.gameDeleted(gameId);
    postNotification(
      NotificationType.UPDATE,
      "Game " + gameId + " has been deleted"
    );
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID socketID) {
    localModel.playerJoinedLobby(gameId, socketID);
    String message;
    if (localModel.getGameId().equals(gameId)) {
      message = socketID.toString() + " joined your game";
    } else {
      message = socketID.toString() + " joined game " + gameId;
    }

    postNotification(NotificationType.UPDATE, message);
  }

  @Override
  public void playerLeftLobby(String gameId, UUID socketID) {
    localModel.playerLeftLobby(gameId, socketID);
    String message;

    if (localModel.getGameId().map(gameId::equals).orElse(false)) {
      message = socketID + " left your game";
    } else {
      message = socketID + " left game " + gameId;
    }

    postNotification(NotificationType.UPDATE, message);
  }

  @Override
  public void playerSetToken(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor token
  ) {
    localModel.playerSetToken(gameId, socketID, nickname, token);
    postNotification(
      NotificationType.UPDATE,
      "Player " +
      nickname +
      " has set their token to " +
      token.toString().toLowerCase()
    );
  }

  @Override
  public void playerSetNickname(String gameId, UUID socketID, String nickname) {
    localModel.playerSetNickname(gameId, socketID, nickname);
    postNotification(
      NotificationType.UPDATE,
      "Player " + nickname + " has set their nickname"
    );
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID socketID,
    String nickname
  ) {
    localModel.playerChoseObjectiveCard(gameId, socketID, nickname);
    postNotification(
      NotificationType.UPDATE,
      "Player " + nickname + " has chosen their objective card"
    );
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
    postNotification(
      NotificationType.UPDATE,
      "Player " +
      nickname +
      " has joined the game with token " +
      color.toString().toLowerCase()
    );
  }

  @Override
  public void gameStarted(String gameId, GameInfo gameInfo) {
    getLocalModel().getClientContextContainer().set(ClientContext.GAME);
    localModel.gameStarted(gameId, gameInfo);
    postNotification(
      NotificationType.UPDATE,
      "Game " + gameId + " has started"
    );
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

    postNotification(
      NotificationType.UPDATE,
      "It's " +
      playerNickname +
      "'s turn" +
      (isLastRound ? " (last round)" : "")
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

    postNotification(
      NotificationType.UPDATE,
      "It's " +
      playerNickname +
      "'s turn" +
      (isLastRound ? " (last round)" : "")
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
  }

  @Override
  public void gameOver() {
    localModel.gameOver();
    postNotification(NotificationType.UPDATE, "Game over");
  }

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores) {
    localModel.playerScoresUpdate(newScores);
  }

  @Override
  public void remainingRounds(String gameID, int remainingRounds) {
    localModel.remainingRounds(gameID, remainingRounds);
    postNotification(NotificationType.UPDATE, remainingRounds + " rounds left");
  }

  @Override
  public void winningPlayer(String nickname) {
    localModel.winningPlayer(nickname);
    postNotification(NotificationType.UPDATE, nickname + " has won the game!");
  }

  @Override
  public void playerConnectionChanged(
    UUID socketID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    localModel.playerConnectionChanged(socketID, nickname, status);
    postNotification(
      NotificationType.UPDATE,
      "Player " +
      nickname +
      " has " +
      status.toString().toLowerCase() +
      " the game"
    );
  }

  @Override
  public void lobbyInfo(LobbyUsersInfo usersInfo) {
    localModel.lobbyInfo(usersInfo);
  }

  @Override
  public void chatMessage(String gameID, ChatMessage message) {
    localModel.chatMessage(gameID, message);
  }

  public void printPrompt() {
    System.out.print("\r> ");
  }

  public void printUpdate(String string) {
    System.out.println(
      "\r" +
      string +
      " ".repeat(string.length() <= 100 ? 100 - string.length() : 0)
    );
    printPrompt();
  }
}
