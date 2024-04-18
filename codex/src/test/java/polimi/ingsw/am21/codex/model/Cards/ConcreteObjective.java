package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.Cards.Objectives.Objective;
import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.function.BiFunction;

public class ConcreteObjective extends Objective {
  public ConcreteObjective() {
    super();
  }

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return ((playerBoard, integer) -> 5);
  }
}
