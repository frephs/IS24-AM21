package polimi.ingsw.am21.codex.controller.messages.serverErrors.game;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class InvalidCardPlacementMessage extends ErrorMessage {

  public int cardId;
  public String reason;

  public InvalidCardPlacementMessage(String reason) {
    super(MessageType.INVALID_CARD_PLACEMENT);
    this.reason = reason;
  }

  @Override
  public String toString() {
    return getType() + "{" + "cardId=" + cardId + ", reason=" + reason + '}';
  }
}
