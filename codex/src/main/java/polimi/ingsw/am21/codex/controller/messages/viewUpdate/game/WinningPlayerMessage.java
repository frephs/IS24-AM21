package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class WinningPlayerMessage extends ViewUpdatingMessage {

  private final String winnerNickname;

  public WinningPlayerMessage(String winnerNickname) {
    super(MessageType.WINNING_PLAYER);
    this.winnerNickname = winnerNickname;
  }

  public String getWinnerNickname() {
    return winnerNickname;
  }

  @Override
  public String toString() {
    return getType() + "{" + "winnerNickname='" + winnerNickname + '\'' + '}';
  }
}
