package polimi.ingsw.am21.codex.controller.messages.clientActions;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class ConnectMessage extends ActionMessage {

  public ConnectMessage(UUID connectionID) {
    super(MessageType.CONNECT, connectionID);
  }

  @Override
  public String toString() {
    return (getType() + "{" + '}');
  }
}
