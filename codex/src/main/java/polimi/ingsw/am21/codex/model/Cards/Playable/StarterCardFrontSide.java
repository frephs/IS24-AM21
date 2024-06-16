package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.HashMap;
import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public class StarterCardFrontSide extends PlayableFrontSide {

  public StarterCardFrontSide() {}

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return ((playerBoard, integer) -> 0);
  }

  @Override
  public String cardToString() {
    // TODO: Implement TUI method
    return "";
  }

  @Override
  public String cardToAscii(HashMap<Integer, String> cardStringMap) {
    return super.cardToAscii(cardStringMap);
  }
}
