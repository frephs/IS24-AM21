package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public class PlayableBackSide extends PlayableSide {

  /**
   * The list of permanent resources on the side
   */
  private final List<ResourceType> permanentResources;

  /**
   * Constructor
   * @param permanentResources The list of permanent resources on the side
   */
  public PlayableBackSide(List<ResourceType> permanentResources) {
    super();
    this.permanentResources = new ArrayList<>(permanentResources);
  }

  public List<ResourceType> getPermanentResources() {
    return permanentResources;
  }

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return ((playerBoard, coveredCorners) -> 0);
  }
}
