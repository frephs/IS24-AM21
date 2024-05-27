package polimi.ingsw.am21.codex.model.Cards;

import java.util.Map;
import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Cards.Objectives.Objective;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public class ConcreteObjective extends Objective {

  public ConcreteObjective() {
    super();
  }

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return ((playerBoard, integer) -> 5);
  }

  @Override
  public String cardToString() {
    return "";
  }

  @Override
  public String cardToAscii() {
    return super.cardToAscii();
  }

  @Override
  public String cardToAscii(Map<Integer, String> cardStringMap) {
    return "";
  }
}
