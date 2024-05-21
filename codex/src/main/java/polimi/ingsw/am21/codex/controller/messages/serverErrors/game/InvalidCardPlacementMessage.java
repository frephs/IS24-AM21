package polimi.ingsw.am21.codex.controller.messages.serverErrors.game;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class InvalidCardPlacementMessage extends ErrorMessage {

  private String reason;

  public InvalidCardPlacementMessage(String reason) {
    super(MessageType.INVALID_CARD_PLACEMENT);
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

  @Override
  public String toString() {
    return getType() + "{" + "cardId=" + ", reason=" + reason + '}';
  }
}
