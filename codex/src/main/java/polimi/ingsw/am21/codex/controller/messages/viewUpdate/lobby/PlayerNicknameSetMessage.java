package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerNicknameSetMessage extends ViewUpdatingMessage {

  public String nickname;

  public PlayerNicknameSetMessage() {
    super(MessageType.PLAYER_NICKNAME_SET);
  }
}
