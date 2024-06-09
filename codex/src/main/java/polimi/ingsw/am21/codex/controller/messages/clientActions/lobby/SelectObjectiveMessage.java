package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class SelectObjectiveMessage extends ActionMessage {

  private final boolean first;
  private final String lobbyId;

  public SelectObjectiveMessage(
    UUID connectionID,
    boolean first,
    String lobbyId
  ) {
    super(MessageType.SELECT_OBJECTIVE, connectionID);
    this.first = first;
    this.lobbyId = lobbyId;
  }

  public boolean isFirst() {
    return first;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  @Override
  public String toString() {
    return (
      getType() + "{" + "first=" + first + ", lobbyId='" + lobbyId + '\'' + '}'
    );
  }
}
