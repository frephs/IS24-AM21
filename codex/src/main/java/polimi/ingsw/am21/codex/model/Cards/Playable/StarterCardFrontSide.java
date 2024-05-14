package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.Map;
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

  public String cardToAscii(Map<Integer, String> cardStringMap) {
    return super.cardToAscii(cardStringMap);
  }
}
