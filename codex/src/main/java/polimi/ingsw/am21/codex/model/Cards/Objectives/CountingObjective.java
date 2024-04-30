package polimi.ingsw.am21.codex.model.Cards.Objectives;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public class CountingObjective extends Objective {

  /**
   * The map of which resources and how many to complete the objective
   */
  private Map<ResourceType, Integer> resources;
  /**
   * The map of which object and how many to complete the objective
   */
  private Map<ObjectType, Integer> objects;

  public CountingObjective(
    Map<ResourceType, Integer> resources,
    Map<ObjectType, Integer> objects
  ) {
    this.resources = new HashMap<>(resources);
    this.objects = new HashMap<>(objects);
  }

  /**
   * The function get the PlayerBoard and the points of the objective card you are evaluating
   * @return the point that you get from that objective card
   */
  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return (playerBoard, points) -> {
      return (
        resources
            .entrySet()
            .stream()
            .map(resource -> {
              return (
                playerBoard.getResources().get(resource.getKey()) /
                resource.getValue()
              );
            })
            .reduce(Integer::min)
            .orElse(0) *
          points +
        (objects
            .entrySet()
            .stream()
            .map(object -> {
              return (
                playerBoard.getObjects().get(object.getKey()) /
                object.getValue()
              );
            })
            .reduce(Integer::min)
            .orElse(0) *
          points)
      );
    };
  }
}
