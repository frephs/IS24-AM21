package polimi.ingsw.am21.codex.connection.server.TCP;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.game.CardPlacedMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby.GameCreatedMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby.GameDeletedMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby.GameStartedMessage;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class TCPControllerListener implements GameEventListener {

  private final Consumer<Message> broadcast;

  public TCPControllerListener(Consumer<Message> broadcast) {
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
    Map<ObjectType, Integer> updatedObjects
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
        updatedObjects
      )
    );
  }

  @Override
  public void changeTurn(
    String gameId,
    Integer nextPlayer,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Boolean isLastRound
  ) {
    // TODO
  }

  @Override
  public void changeTurn(
    String gameId,
    Integer nextPlayer,
    Boolean isLastRound
  ) {
    // TODO
  }

  @Override
  public void gameStarted(String gameId, List<String> players) {
    broadcast.accept(new GameStartedMessage(gameId, players));
  }

  @Override
  public void gameCreated(String gameId, int players) {
    broadcast.accept(new GameCreatedMessage(gameId, players));
  }

  @Override
  public void gameDeleted(String gameId) {
    broadcast.accept(new GameDeletedMessage(gameId));
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID socketID) {
    // TODO
  }

  @Override
  public void playerLeftLobby(String gameId, UUID socketID) {
    // TODO
  }

  @Override
  public void playerSetToken(String gameId, UUID socketID, TokenColor token) {
    // TODO
  }

  @Override
  public void playerSetNickname(String gameId, UUID socketID, String nickname) {
    // TODO
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID socketID,
    Boolean isFirst
  ) {
    // TODO
  }

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs
  ) {
    // TODO
  }
}
