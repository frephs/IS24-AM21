package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.HashMap;
import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;

public class StarterCardFrontSide extends PlayableFrontSide {

  public StarterCardFrontSide() {}

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return ((playerBoard, integer) -> 0);
  }

  @Override
  public String cardToString(Cli.Options options) {
    // TODO: Implement TUI method
    return "";
  }

  @Override
  public String cardToAscii(
    Cli.Options options,
    HashMap<Integer, String> cardStringMap
  ) {
    return super.cardToAscii(options, cardStringMap);
  }
}
