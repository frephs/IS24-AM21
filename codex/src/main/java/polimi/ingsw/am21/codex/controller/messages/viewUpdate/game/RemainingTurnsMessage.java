package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class RemainingTurnsMessage extends ViewUpdatingMessage {

  public int turns;

  public RemainingTurnsMessage() {
    super(MessageType.REMAINING_TURNS);
  }
}
