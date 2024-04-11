package polimi.ingsw.am21.codex.model.Cards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import polimi.ingsw.am21.codex.model.PlayerBoard;

public class GeometricObjective extends Objective {

  private Map<AdjacentPosition, ResourceType> geometry = new HashMap<>();

  public GeometricObjective(Map<AdjacentPosition, ResourceType> geometry) {
    this.geometry = geometry;
  }

  @Override
  public Function<PlayerBoard, Integer> getEvaluator() {
    //deve ritornare quanti punti
  }
}
