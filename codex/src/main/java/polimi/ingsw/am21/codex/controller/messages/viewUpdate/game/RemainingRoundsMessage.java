package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class RemainingRoundsMessage extends ViewUpdatingMessage {

  private final Integer rounds;
  private final String gameID;

  public RemainingRoundsMessage(String gameID, Integer rounds) {
    super(MessageType.REMAINING_ROUNDS);
    this.gameID = gameID;
    this.rounds = rounds;
  }

  public String getGameID() {
    return gameID;
  }

  public Integer getRounds() {
    return rounds;
  }

  @Override
  public String toString() {
    return getType() + "{" + "gameID=" + gameID + ", rounds=" + rounds + '}';
  }
}
