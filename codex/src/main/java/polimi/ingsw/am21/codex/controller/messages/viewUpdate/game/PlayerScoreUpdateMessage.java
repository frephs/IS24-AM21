package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerScoreUpdateMessage extends ViewUpdatingMessage {

  private final String nickname;
  private final int delta;

  public PlayerScoreUpdateMessage(String nickname, int delta) {
    super(MessageType.PLAYER_SCORE_UPDATE);
    this.nickname = nickname;
    this.delta = delta;
  }

  public String getNickname() {
    return nickname;
  }

  public int getDelta() {
    return delta;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "nickname='" +
      nickname +
      '\'' +
      ", delta=" +
      delta +
      '}'
    );
  }
}
