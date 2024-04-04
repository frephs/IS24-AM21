package polimi.ingsw.am21.codex.model.Cards;

import java.util.List;
import java.util.function.Function;
import polimi.ingsw.am21.codex.model.PlayerBoard;

public class GeometricObjective extends Objective {

  private List<List<ResourceType>> geometry;

  public GeometricObjective(List<List<ResourceType>> geometry) {
    this.geometry = geometry;
  }

  @Override
  public Function<PlayerBoard, Integer> getEvaluator() {
    //deve ritornare quanti punti
  }
}
