package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class SelectFromPairMessage extends ActionMessage {

  private final boolean first;
  private final String lobbyId;

  public SelectFromPairMessage(boolean first, String lobbyId) {
    super(MessageType.SELECT_FROM_PAIR);
    this.first = first;
    this.lobbyId = lobbyId;
  }

  public boolean isFirst() {
    return first;
  }

  public String getLobbyId() {
    return lobbyId;
  }
}
