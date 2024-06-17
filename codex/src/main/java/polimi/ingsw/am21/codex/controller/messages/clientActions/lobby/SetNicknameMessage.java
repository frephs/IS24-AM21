package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class SetNicknameMessage extends ActionMessage {

  private final String nickname;
  private final String lobbyId;

  public SetNicknameMessage(
    UUID connectionID,
    String nickname,
    String lobbyId
  ) {
    super(MessageType.SET_NICKNAME, connectionID);
    this.nickname = nickname;
    this.lobbyId = lobbyId;
  }

  public String getNickname() {
    return nickname;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "nickname='" +
      nickname +
      '\'' +
      ", lobbyId='" +
      lobbyId +
      '\'' +
      '}'
    );
  }
}
