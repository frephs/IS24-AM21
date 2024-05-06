package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class SetNicknameMessage extends ActionMessage {

  public String nickname;

  public SetNicknameMessage() {
    super(MessageType.SET_NICKNAME);
  }
}
