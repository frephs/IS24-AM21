package polimi.ingsw.am21.codex.model.Cards;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;

/** This is a utility class that implements a bare-bone card: it's meant to exist for tests only. */
class ConcreteCard extends Card {

  public ConcreteCard(int id) {
    super(id);
  }

  @Override
  public Function<PlayerBoard, Integer> getEvaluator() {
    return pb -> 123;
  }

  @Override
  public String cardToString() {
    return "";
  }

  public String cardToAscii(HashMap<Integer, String> cardStringMap) {
    return "";
  }
}
