package polimi.ingsw.am21.codex.controller.messages.serverErrors;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class NotAClientMessageMessage extends ErrorMessage {

  public NotAClientMessageMessage() {
    super(MessageType.NOT_A_CLIENT_MESSAGE);
  }
}
