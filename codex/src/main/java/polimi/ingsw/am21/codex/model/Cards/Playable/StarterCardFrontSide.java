package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public class StarterCardFrontSide extends PlayableFrontSide {

  public StarterCardFrontSide() {}

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return ((playerBoard, integer) -> 0);
  }
}
