package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;

public class CardPlacedMessage extends ViewUpdatingMessage {

  private final String playerId;
  private final int cardId;
  private final CardSideType side;
  private final Position position;

  public CardPlacedMessage(
    String playerId,
    int cardId,
    CardSideType side,
    Position position
  ) {
    super(MessageType.CARD_PLACED);
    this.playerId = playerId;
    this.position = position;
    this.cardId = cardId;
    this.side = side;
  }

  public String getPlayerId() {
    return playerId;
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

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "position=" +
      position +
      ", cardId=" +
      cardId +
      ", side=" +
      side +
      ", playerId='" +
      playerId +
      '}'
    );
  }
}
