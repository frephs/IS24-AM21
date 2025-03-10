package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;

public class CardPlacedMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final String playerId;
  private final Integer playerHandCardNumber;
  private final Integer cardId;
  private final CardSideType side;
  private final Position position;
  private final Integer newPlayerScore;
  private final Map<ResourceType, Integer> updatedResources;
  private final Map<ObjectType, Integer> updatedObjects;
  private final Set<Position> availableSpots;
  private final Set<Position> forbiddenSpots;

  public CardPlacedMessage(
    String gameId,
    String playerId,
    Integer playerHandCardNumber,
    Integer cardId,
    CardSideType side,
    Position position,
    Integer newPlayerScore,
    Map<ResourceType, Integer> updatedResources,
    Map<ObjectType, Integer> updatedObjects,
    Set<Position> availablePositions,
    Set<Position> forbiddenPositions
  ) {
    super(MessageType.CARD_PLACED);
    this.gameId = gameId;
    this.playerId = playerId;
    this.playerHandCardNumber = playerHandCardNumber;
    this.cardId = cardId;
    this.side = side;
    this.position = position;
    this.newPlayerScore = newPlayerScore;
    this.updatedResources = updatedResources == null
      ? Map.of()
      : new HashMap<>(updatedResources);
    this.updatedObjects = updatedObjects == null
      ? Map.of()
      : new HashMap<>(updatedObjects);
    this.availableSpots = availablePositions;
    this.forbiddenSpots = forbiddenPositions;
  }

  public String getGameId() {
    return gameId;
  }

  public String getPlayerId() {
    return playerId;
  }

  public int getPlayerHandCardNumber() {
    return playerHandCardNumber;
  }

  public int getCardId() {
    return cardId;
  }

  public CardSideType getSide() {
    return side;
  }

  public Position getPosition() {
    return position;
  }

  public int getNewPlayerScore() {
    return newPlayerScore;
  }

  public Map<ResourceType, Integer> getUpdatedResources() {
    return updatedResources;
  }

  public Map<ObjectType, Integer> getUpdatedObjects() {
    return updatedObjects;
  }

  public Set<Position> getAvailableSpots() {
    return availableSpots;
  }

  public Set<Position> getForbiddenSpots() {
    return forbiddenSpots;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId='" +
      gameId +
      '\'' +
      ", playerId='" +
      playerId +
      '\'' +
      ", playerHandCardNumber=" +
      playerHandCardNumber +
      ", cardId=" +
      cardId +
      ", side=" +
      side +
      ", position=" +
      position +
      ", newPlayerScore=" +
      newPlayerScore +
      ", updatedResources=" +
      updatedResources +
      ", updatedObjects=" +
      updatedObjects +
      ", availablePositions=" +
      availableSpots +
      ", forbiddenPositions=" +
      forbiddenSpots +
      '}'
    );
  }
}
