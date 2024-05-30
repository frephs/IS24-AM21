package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class StarterCardSidesMessage extends ResponseMessage {

  private final int cardId;

  public StarterCardSidesMessage(int cardId) {
    super(MessageType.STARTER_CARD_SIDES);
    this.cardId = cardId;
  }

  public int getCardId() {
    return cardId;
  }

  @Override
  public String toString() {
    return getType() + "{" + "cardId=" + cardId + '}';
  }
}
