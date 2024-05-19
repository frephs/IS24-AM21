package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class GameOverMessage extends ViewUpdatingMessage {

  public GameOverMessage() {
    super(MessageType.GAME_OVER);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
