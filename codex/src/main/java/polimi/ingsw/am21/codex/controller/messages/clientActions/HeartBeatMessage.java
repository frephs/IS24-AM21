package polimi.ingsw.am21.codex.controller.messages.clientActions;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class HeartBeatMessage extends ActionMessage {

  public HeartBeatMessage(UUID connectionID) {
    super(MessageType.HEART_BEAT, connectionID);
  }

  @Override
  public String toString() {
    return (getType() + "{" + '}');
  }
}
