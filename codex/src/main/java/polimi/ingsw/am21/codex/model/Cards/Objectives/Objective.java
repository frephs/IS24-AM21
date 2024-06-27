package polimi.ingsw.am21.codex.model.Cards.Objectives;

import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.CliCard;

public abstract class Objective implements CliCard {

  /**
   * Function that takes in a playerboard and the points of the objective card and
   * returns the rewarded points
   */
  public abstract BiFunction<PlayerBoard, Integer, Integer> getEvaluator();
}
