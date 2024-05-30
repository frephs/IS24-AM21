package polimi.ingsw.am21.codex.model.Cards.Objectives;

import java.util.Map;
import java.util.function.Function;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.CliCard;

public class ObjectiveCard extends Card implements CliCard {

  /**
   * The points that the objective can give
   */
  private int points;
  /**
   * The objective of the card
   */
  private Objective objective;

  public ObjectiveCard(int id, int points, Objective objective) {
    super(id);
    this.points = points;
    this.objective = objective;
  }

  /**
   * Take the PlayerBoard of player
   * @return The points of the objective card
   */
  @Override
  public Function<PlayerBoard, Integer> getEvaluator() {
    return playerBoard -> {
      return objective.getEvaluator().apply(playerBoard, points);
    };
  }

  /*
   * -----------------
   * TUI METHODS
   * -----------------
   * */

  @Override
  public String cardToString() {
    return (
      "Gain " +
      points +
      ((points > 1) ? " points" : " point") +
      " for each " +
      objective.cardToString()
    );
  }

  public String cardToAscii(Map<Integer, String> cardStringMap) {
    // TODO: Implement TUI method
    return "";
  }
}
