package polimi.ingsw.am21.codex.connection.server.TCP;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.game.CardPlacedMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.game.NextTurnUpdateMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.game.PlayerJoinedGameMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby.*;
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
  public void changeTurn(
    String gameId,
    String playerId,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Integer newPairCardId
  ) {
    broadcast.accept(
      new NextTurnUpdateMessage(
        gameId,
        playerId,
        source,
        deck,
        cardId,
        newPairCardId
      )
    );
  }

  @Override
  public void changeTurn(String gameId, String playerId, Boolean isLastRound) {
    broadcast.accept(new NextTurnUpdateMessage(gameId, playerId));
  }

  @Override
  public void gameStarted(String gameId, List<String> players) {
    broadcast.accept(new GameStartedMessage(gameId, players));
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    broadcast.accept(
      new GameCreatedMessage(gameId, currentPlayers, maxPlayers)
    );
  }

  @Override
  public void gameDeleted(String gameId) {
    broadcast.accept(new GameDeletedMessage(gameId));
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID socketID) {
    broadcast.accept(new PlayerJoinedLobbyMessage(gameId, socketID));
  }

  @Override
  public void playerLeftLobby(String gameId, UUID socketID) {
    broadcast.accept(new PlayerLeftLobbyMessage(gameId, socketID));
  }

  @Override
  public void playerSetToken(String gameId, UUID socketID, TokenColor token) {
    broadcast.accept(new PlayerSetTokenColorMessage(gameId, socketID, token));
  }

  @Override
  public void playerSetNickname(String gameId, UUID socketID, String nickname) {
    broadcast.accept(new PlayerSetNicknameMessage(gameId, socketID, nickname));
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID socketID,
    Boolean isFirst
  ) {
    // TODO Do we need to add a broadcast message for this? Do other clients care?
  }

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs
  ) {
    broadcast.accept(
      new PlayerJoinedGameMessage(gameId, socketID, nickname, color, handIDs)
    );
  }
}
