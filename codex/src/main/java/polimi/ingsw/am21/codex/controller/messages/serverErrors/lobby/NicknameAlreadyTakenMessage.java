package polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class NicknameAlreadyTakenMessage extends ErrorMessage {

  public NicknameAlreadyTakenMessage() {
    super(MessageType.NICKNAME_ALREADY_TAKEN);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
