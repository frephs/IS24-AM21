package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class SelectFromPairMessage extends ActionMessage {

  public boolean first;

  public SelectFromPairMessage() {
    super(MessageType.SELECT_FROM_PAIR);
  }
}
