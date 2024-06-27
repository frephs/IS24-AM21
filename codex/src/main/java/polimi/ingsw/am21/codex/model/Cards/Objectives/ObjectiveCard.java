package polimi.ingsw.am21.codex.model.Cards.Objectives;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliCard;

public class ObjectiveCard extends Card implements CliCard {

  /**
   * The points that the objective can give
   */
  private final int points;
  /**
   * The objective of the card
   */
  private final Objective objective;

  public ObjectiveCard(int id, int points, Objective objective) {
    super(id);
    this.points = points;
    this.objective = objective;
  }

  @Override
  public Function<PlayerBoard, Integer> getEvaluator() {
    return playerBoard -> {
      return objective.getEvaluator().apply(playerBoard, points);
    };
  }

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

  @Override
  public String cardToAscii(HashMap<Integer, String> cardStringMap) {
    return cardToString();
  }
}
