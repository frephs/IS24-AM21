package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class LeaveLobbyMessage extends ActionMessage {

  public LeaveLobbyMessage() {
    super(MessageType.LEAVE_LOBBY);
  }

  @Override
  public String toString() {
    return (getType()).toString();
  }
}
