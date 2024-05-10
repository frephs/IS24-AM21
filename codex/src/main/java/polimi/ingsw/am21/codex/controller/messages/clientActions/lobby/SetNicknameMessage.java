package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class SetNicknameMessage extends ActionMessage {

  private final String nickname;
  private final String lobbyId;

  public SetNicknameMessage(String nickname, String lobbyId) {
    super(MessageType.SET_NICKNAME);
    this.nickname = nickname;
    this.lobbyId = lobbyId;
  }

  public String getNickname() {
    return nickname;
  }

  public String getLobbyId() {
    return lobbyId;
  }
}
