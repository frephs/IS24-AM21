package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class AvailableTokenColorsMessage extends ResponseMessage {

  public AvailableTokenColorsMessage() {
    super(MessageType.AVAILABLE_TOKEN_COLORS);
  }
}
