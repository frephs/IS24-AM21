package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class WinningPlayerMessage extends ViewUpdatingMessage {

  public String winnerNickname;

  public WinningPlayerMessage() {
    super(MessageType.WINNING_PLAYER);
  }
}
