package polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class NicknameAlreadyTakenMessage extends ErrorMessage {

  private final String nickname;

  public NicknameAlreadyTakenMessage(String nickname) {
    super(MessageType.NICKNAME_ALREADY_TAKEN);
    this.nickname = nickname;
  }

  public String getNickname() {
    return nickname;
  }

  @Override
  public String toString() {
    return getType().toString() + "{ nickname=" + nickname + " }";
  }
}
