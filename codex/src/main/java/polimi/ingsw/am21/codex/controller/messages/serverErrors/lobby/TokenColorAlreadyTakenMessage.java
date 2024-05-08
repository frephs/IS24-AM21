package polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class TokenColorAlreadyTakenMessage extends ErrorMessage {

  public TokenColorAlreadyTakenMessage() {
    super(MessageType.TOKEN_COLOR_ALREADY_TAKEN);
  }
}
