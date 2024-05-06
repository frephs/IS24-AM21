package polimi.ingsw.am21.codex.controller.messages.serverErrors;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class ActionNotAllowedMessage extends ErrorMessage {

  public ActionNotAllowedMessage() {
    super(MessageType.ACTION_NOT_ALLOWED);
  }
}
