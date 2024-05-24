package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class RemainingTurnsMessage extends ViewUpdatingMessage {

  private final Integer turns;

  public RemainingTurnsMessage(Integer turns) {
    super(MessageType.REMAINING_TURNS);
    this.turns = turns;
  }

  public int getTurns() {
    return turns;
  }

  @Override
  public String toString() {
    return getType() + "{" + "turns=" + turns + '}';
  }
}
