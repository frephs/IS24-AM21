package polimi.ingsw.am21.codex.controller.messages.serverErrors;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class UnknownMessageTypeMessage extends ErrorMessage {

  public UnknownMessageTypeMessage() {
    super(MessageType.UNKNOWN_MESSAGE_TYPE);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
