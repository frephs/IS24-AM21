package polimi.ingsw.am21.codex.client;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.remote.LocalModelGameEventListener;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.controller.listeners.*;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameAlreadyExistsException;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.View;

/**
 * Class that handles the events thrown by the server, caught by the clients and forwards them to the local model and the view.
 * It implements the GameEventListener and GameErrorListener interfaces.
 * @see GameEventListener
 * @see GameErrorListener
 * */
public class ClientGameEventHandler
  implements GameEventListener, GameErrorListener {

  /**
   * The listener to propagate events to
   */
  private final RemoteGameEventListener listener;

  /**
   * The local model container of the handler's client
   */
  protected LocalModelContainer localModel;

  /**
   * The view the associated client is using
   */
  private final View view;

  public View getView() {
    return view;
  }

  public ClientGameEventHandler(View view, LocalModelContainer localModel) {
    this.localModel = localModel;

    try {
      listener = new LocalModelGameEventListener(this);
    } catch (RemoteException e) {
      throw new RuntimeException("Failed creating client", e);
    }

    this.view = view;
  }

  public RemoteGameEventListener getRemoteListener() {
    return listener;
  }

  public LocalModelContainer getLocalModel() {
    return localModel;
  }

  public void handleInvalidActionException(InvalidActionException e) {
    switch (e.getCode()) {
      case PLAYER_NOT_ACTIVE -> this.playerNotActive();
      case NOT_IN_GAME -> this.notInGame();
      case GAME_ALREADY_STARTED -> this.gameAlreadyStarted();
      case GAME_ALREADY_EXISTS -> this.gameAlreadyExists(
          ((GameAlreadyExistsException) e).getGameID()
        );
      case INVALID_NEXT_TURN_CALL -> this.invalidNextTurnCall();
      case INVALID_GET_OBJECTIVE_CARDS_CALL -> this.invalidGetObjectiveCardsCall();
      case GAME_NOT_READY -> this.gameNotReady();
      case GAME_NOT_FOUND -> this.gameNotFound(
          ((GameNotFoundException) e).getGameID()
        );
      case PLAYER_NOT_FOUND -> this.playerNotFound();
      case INCOMPLETE_LOBBY_PLAYER -> this.incompleteLobbyPlayer(
          e.getNotes().get(0)
        );
      case EMPTY_DECK -> this.emptyDeck();
      case ALREADY_PLACED_CARD -> this.alreadyPlacedCard();
      case ILLEGAL_PLACING_POSITION -> this.invalidCardPlacement(
          ((IllegalPlacingPositionException) e).getReason()
        );
      case ILLEGAL_CARD_SIDE_CHOICE -> this.invalidCardPlacement(
          e.getMessage()
        );
      case LOBBY_FULL -> this.lobbyFull(((LobbyFullException) e).getGameID());
      case NICKNAME_ALREADY_TAKEN -> this.nicknameTaken(
          ((NicknameAlreadyTakenException) e).getNickname()
        );
      case INVALID_TOKEN_COLOR -> this.invalidTokenColor();
      case TOKEN_ALREADY_TAKEN -> this.tokenTaken(
          TokenColor.fromString(
            ((TokenAlreadyTakenException) e).getTokenColor()
          )
        );
      case GAME_OVER -> this.gameOver();
      case CARD_NOT_PLACED -> this.cardNotPlaced();
    }
  }

  public void listGames() {
    view.listGames();
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    localModel.gameCreated(gameId, currentPlayers, maxPlayers);
    view.gameCreated(gameId, currentPlayers, maxPlayers);
  }

  @Override
  public void refreshLobbies(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {
    localModel.refreshLobbies(lobbyIds, currentPlayers, maxPlayers);
    view.refreshLobbies(lobbyIds, currentPlayers, maxPlayers);
  }

  @Override
  public void gameDeleted(String gameId) {
    localModel.gameDeleted(gameId);
    view.gameDeleted(gameId);
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID connectionID) {
    localModel.playerJoinedLobby(gameId, connectionID);
    view.playerJoinedLobby(gameId, connectionID);
  }

  @Override
  public void playerLeftLobby(String gameId, UUID connectionID) {
    localModel.playerLeftLobby(gameId, connectionID);
    view.playerLeftLobby(gameId, connectionID);
  }

  @Override
  public void playerSetToken(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor token
  ) {
    localModel.playerSetToken(gameId, connectionID, nickname, token);
    view.playerSetToken(gameId, connectionID, nickname, token);
  }

  @Override
  public void playerSetNickname(
    String gameId,
    UUID connectionID,
    String nickname
  ) {
    localModel.playerSetNickname(gameId, connectionID, nickname);
    view.playerSetNickname(gameId, connectionID, nickname);
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID connectionID,
    String nickname
  ) {
    localModel.playerChoseObjectiveCard(gameId, connectionID, nickname);
    view.playerChoseObjectiveCard(gameId, connectionID, nickname);
  }

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardID,
    CardSideType starterSide
  ) {
    localModel.playerJoinedGame(
      gameId,
      connectionID,
      nickname,
      color,
      handIDs,
      starterCardID,
      starterSide
    );
    view.playerJoinedGame(
      gameId,
      connectionID,
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
  public void unknownResponse() {
    view.postNotification(
      NotificationType.ERROR,
      "An unknown error occurred. Please try again."
    );
  }

  @Override
  public void gameAlreadyExists(String gameId) {
    view.postNotification(
      NotificationType.ERROR,
      "A game called " + gameId + " already exists."
    );
  }

  @Override
  public void gameNotFound(String gameId) {
    localModel.gameDeleted(gameId);
    view.postNotification(
      NotificationType.ERROR,
      "Game " + gameId + " not found."
    );
  }

  @Override
  public void notInLobby() {
    view.postNotification(
      NotificationType.ERROR,
      "You are not in any lobby yet."
    );
  }

  @Override
  public void lobbyFull(String gameId) {
    localModel.lobbyFull(gameId);
    view.postNotification(
      NotificationType.ERROR,
      "The lobby for game " + gameId + " is full."
    );
  }

  @Override
  public void tokenTaken(TokenColor token) {
    localModel.tokenTaken(token);
    view.postNotification(
      NotificationType.ERROR,
      new String[] { "Token ", " is already taken." },
      token,
      1
    );
  }

  @Override
  public void nicknameTaken(String nickname) {
    view.postNotification(
      NotificationType.ERROR,
      "The nickname " + nickname + " is already taken."
    );
  }

  @Override
  public void gameNotStarted() {
    view.postNotification(
      NotificationType.ERROR,
      "The game has not started yet."
    );
  }

  @Override
  public void notInGame() {
    view.postNotification(
      NotificationType.ERROR,
      "You are not in any game yet."
    );
  }

  @Override
  public void gameAlreadyStarted() {
    view.postNotification(
      NotificationType.ERROR,
      "The game has already started."
    );
  }

  @Override
  public void playerNotActive() {
    view.postNotification(NotificationType.ERROR, "It's not your turn.");
  }

  @Override
  public void invalidCardPlacement(String reason) {
    view.postNotification(NotificationType.ERROR, reason);
  }

  @Override
  public void invalidNextTurnCall() {
    view.postNotification(NotificationType.ERROR, "Invalid next turn call.");
  }

  @Override
  public void invalidGetObjectiveCardsCall() {
    view.postNotification(
      NotificationType.ERROR,
      "Invalid get objective cards call."
    );
  }

  @Override
  public void gameNotReady() {
    view.postNotification(NotificationType.ERROR, "The game is not ready yet.");
  }

  @Override
  public void gameOver() {
    localModel.gameOver();
    view.gameOver();
  }

  @Override
  public void emptyDeck() {
    view.postNotification(NotificationType.WARNING, "The deck is empty.");
  }

  @Override
  public void playerNotFound() {
    view.postNotification(NotificationType.ERROR, "Player not found.");
  }

  @Override
  public void incompleteLobbyPlayer(String msg) {
    view.postNotification(NotificationType.ERROR, msg);
  }

  @Override
  public void illegalCardSideChoice() {
    view.postNotification(
      NotificationType.WARNING,
      "Illegal card side choice."
    );
  }

  @Override
  public void invalidTokenColor() {
    view.postNotification(NotificationType.ERROR, "Invalid token color.");
  }

  @Override
  public void alreadyPlacedCard() {
    view.postNotification(
      NotificationType.WARNING,
      "You have already placed a card this turn."
    );
  }

  //TODO remove this
  @Override
  public void cardNotPlaced() {
    view.postNotification(NotificationType.ERROR, "Card not placed.");
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
    UUID connectionID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    localModel.playerConnectionChanged(connectionID, nickname, status);
    view.playerConnectionChanged(connectionID, nickname, status);
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

  @Override
  public void getObjectiveCards(Pair<Integer, Integer> objectiveCards) {
    localModel.getObjectiveCards(objectiveCards);
    view.getObjectiveCards(objectiveCards);
  }

  @Override
  public void getStarterCard(Integer cardId) {
    localModel.getStarterCard(cardId);
    view.getStarterCard(cardId);
  }

  @Override
  public void gameHalted(String gameID) {
    localModel.gameHalted(gameID);
    view.gameHalted(gameID);
  }

  @Override
  public void gameResumed(String gameID) {
    localModel.gameHalted(gameID);
    view.gameHalted(gameID);
  }

  @Override
  public void userContext(FullUserGameContext context) {
    localModel.userContext(context);
    view.userContext(context);
  }
}
