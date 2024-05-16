package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerNicknameSetMessage extends ViewUpdatingMessage {

  private final String nickname;

  public PlayerNicknameSetMessage(String nickname) {
    super(MessageType.PLAYER_NICKNAME_SET);
    this.nickname = nickname;
  }

  public String getNickname() {
    return nickname;
  }

  @Override
  public String toString() {
    return getType() + "{" + "nickname='" + nickname + '\'' + '}';
  }
}
