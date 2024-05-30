package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import java.util.HashMap;
import java.util.Map;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerScoresUpdateMessage extends ViewUpdatingMessage {

  private final Map<String, Integer> newScores;

  public PlayerScoresUpdateMessage(Map<String, Integer> newScores) {
    super(MessageType.PLAYER_SCORES_UPDATE);
    this.newScores = new HashMap<>(newScores);
  }

  public Map<String, Integer> getNewScores() {
    return newScores;
  }

  @Override
  public String toString() {
    return (getType() + "{" + "newScores='" + newScores + '}');
  }
}
