package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class LeaveLobbyMessage extends ActionMessage {

  public LeaveLobbyMessage(UUID connectionID) {
    super(MessageType.LEAVE_LOBBY, connectionID);
  }

  @Override
  public String toString() {
    return (getType()).toString();
  }
}
