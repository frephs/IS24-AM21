package polimi.ingsw.am21.codex.connection.server.TCP;

import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Consumer;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.FullUserGameContext;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.clientActions.SendChatMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.ChatMessageMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.PlayerConnectionChangedMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.game.*;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby.*;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class TCPServerControllerListener implements GameEventListener {

  /**
   * The consumer to be called to broadcast messages to all players
   */
  private final Consumer<Message> broadcast;

  public TCPServerControllerListener(Consumer<Message> broadcast) {
    this.broadcast = broadcast;
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
    broadcast.accept(
      new CardPlacedMessage(
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
      )
    );
  }

  @Override
  public void gameOver() {
    broadcast.accept(new GameOverMessage());
  }

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores) {
    broadcast.accept(new PlayerScoresUpdateMessage(newScores));
  }

  @Override
  public void remainingRounds(String gameID, int remainingRounds) {
    broadcast.accept(new RemainingRoundsMessage(gameID, remainingRounds));
  }

  @Override
  public void winningPlayer(String nickname) {
    broadcast.accept(new WinningPlayerMessage(nickname));
  }

  @Override
  public void playerConnectionChanged(
    UUID connectionID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    broadcast.accept(
      new PlayerConnectionChangedMessage(connectionID, nickname, status)
    );
  }

  @Override
  public void lobbyInfo(LobbyUsersInfo usersInfo) {
    broadcast.accept(new LobbyInfoMessage(usersInfo));
  }

  @Override
  public void chatMessage(String gameID, ChatMessage message) {
    broadcast.accept(new ChatMessageMessage(gameID, message));
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
    broadcast.accept(
      new NextTurnUpdateMessage(
        gameId,
        playerNickname,
        playerIndex,
        source,
        deck,
        cardId,
        newPairCardId,
        availableSpots,
        forbiddenSpots,
        resourceDeckTopCardId,
        goldDeckTopCardId
      )
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
    broadcast.accept(
      new NextTurnUpdateMessage(
        gameId,
        playerNickname,
        playerIndex,
        availableSpots,
        forbiddenSpots,
        resourceDeckTopCardId,
        goldDeckTopCardId
      )
    );
  }

  @Override
  public void gameStarted(String gameId, GameInfo gameInfo) {
    broadcast.accept(new GameStartedMessage(gameInfo));
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    broadcast.accept(
      new GameCreatedMessage(gameId, currentPlayers, maxPlayers)
    );
  }

  @Override
  public void refreshLobbies(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {}

  @Override
  public void gameDeleted(String gameId) {
    broadcast.accept(new GameDeletedMessage(gameId));
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID connectionID) {
    broadcast.accept(new PlayerJoinedLobbyMessage(gameId, connectionID));
  }

  @Override
  public void playerLeftLobby(String gameId, UUID connectionID) {
    broadcast.accept(new PlayerLeftLobbyMessage(gameId, connectionID));
  }

  @Override
  public void playerSetToken(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor token
  ) {
    broadcast.accept(
      new PlayerSetTokenColorMessage(gameId, connectionID, nickname, token)
    );
  }

  @Override
  public void playerSetNickname(
    String gameId,
    UUID connectionID,
    String nickname
  ) {
    broadcast.accept(
      new PlayerSetNicknameMessage(gameId, connectionID, nickname)
    );
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID connectionID,
    String nickname
  ) {
    broadcast.accept(
      new PlayerChoseObjectiveCardMessage(gameId, connectionID, nickname)
    );
  }

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCard,
    CardSideType starterSide
  ) {
    broadcast.accept(
      new PlayerJoinedGameMessage(
        gameId,
        connectionID,
        nickname,
        color,
        handIDs,
        starterCard,
        starterSide
      )
    );
  }

  @Override
  public void getObjectiveCards(Pair<Integer, Integer> objectiveCards) {}

  @Override
  public void getStarterCard(Integer cardId) {}

  @Override
  public void gameHalted(String gameID) {
    broadcast.accept(new GameHaltedMessage(gameID, true));
  }

  @Override
  public void gameResumed(String gameID) {
    broadcast.accept(new GameHaltedMessage(gameID, false));
  }

  @Override
  public void userContext(FullUserGameContext context) {
    // TODO
    //    broadcast.accept(new UserContextMessage(context));
  }
}
