package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public class ResourceCardFrontSide extends PlayableFrontSide {

  /**
   * The points the card should attribute to the player
   */
  protected final int points;

  /**
   * Constructor
   * @param points The points the card should attribute to the player
   */
  public ResourceCardFrontSide(int points) {
    super();
    this.points = points;
  }

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return ((playerBoard, integer) -> points);
  }
}
