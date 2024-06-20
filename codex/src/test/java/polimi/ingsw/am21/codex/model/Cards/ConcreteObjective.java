package polimi.ingsw.am21.codex.model.Cards;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Cards.Objectives.Objective;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;

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
  public String cardToAscii(HashMap<Integer, String> cardStringMap) {
    return "";
  }
}
