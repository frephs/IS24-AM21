package polimi.ingsw.am21.codex.model.Cards;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.PlayerBoard;

public abstract class PlayableSide {

  /**
   * The Map of the CornerPosition and the corner on the side of the card
   */
  private HashMap<CornerPosition, Corner> corners;

  public HashMap<CornerPosition, Corner> getCorners() {
    return corners;
  }

  public void setCorners(CornerPosition position, CornerContentType content) {
    corners.put(position, new Corner(content));
  }

  /**
   * Generates a function that should be called to get the points that should be
   * attributed to a player when they place a card on this side. The returned
   * function requires a PlayerBoard an Integer representing the number of corners
   * the card is covering.
   */
  public abstract BiFunction<PlayerBoard, Integer, Integer> getEvaluator();
}
