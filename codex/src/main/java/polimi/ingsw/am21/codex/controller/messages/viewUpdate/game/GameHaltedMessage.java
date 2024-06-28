package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class GameHaltedMessage extends ViewUpdatingMessage {

  private String gameID;
  private Boolean halted;

  public GameHaltedMessage(String gameID, Boolean halted) {
    super(MessageType.GAME_HALTED_UPDATE);
    this.gameID = gameID;
    this.halted = halted;
  }

  @Override
  public String toString() {
    return getType().toString();
  }

  public String getGameID() {
    return this.gameID;
  }

  public Boolean getHalted() {
    return halted;
  }
}
