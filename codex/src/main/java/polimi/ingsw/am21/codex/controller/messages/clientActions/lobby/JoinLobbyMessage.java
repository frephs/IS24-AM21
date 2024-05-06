package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class JoinLobbyMessage extends ActionMessage {

  private int lobbyId;

  public JoinLobbyMessage() {
    super(MessageType.JOIN_LOBBY);
  }
}
