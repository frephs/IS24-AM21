package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class JoinLobbyMessage extends ActionMessage {

  private final String lobbyId;

  public JoinLobbyMessage(String lobbyId) {
    super(MessageType.JOIN_LOBBY);
    this.lobbyId = lobbyId;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  @Override
  public String toString() {
    return getType() + "{" + "lobbyId='" + lobbyId + '\'' + '}';
  }
}
