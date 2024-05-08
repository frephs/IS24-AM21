package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerScoreUpdateMessage extends ViewUpdatingMessage {

  public int delta;

  public PlayerScoreUpdateMessage() {
    super(MessageType.PLAYER_SCORE_UPDATE);
  }
}
