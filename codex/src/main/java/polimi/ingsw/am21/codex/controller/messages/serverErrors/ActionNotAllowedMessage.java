package polimi.ingsw.am21.codex.controller.messages.serverErrors;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class ActionNotAllowedMessage extends ErrorMessage {

  private String reason;

  public ActionNotAllowedMessage(String reason) {
    super(MessageType.ACTION_NOT_ALLOWED);
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

  @Override
  public String toString() {
    return "ActionNotAllowedMessage{" + "cause='" + cause + '\'' + '}';
  }
}
