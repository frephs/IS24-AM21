package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class CountingObjective extends Objective{
    /**
     * The map of which resources and how many to complete the objective
     */
    private Map<ResourceType, Integer> resources;
    /**
     * The map of which object and how many to complete the objective
     */
    private Map<ObjectType, Integer> objects;

    public CountingObjective(Map<ResourceType, Integer> resources, Map<ObjectType, Integer> objects) {
        this.resources = new HashMap<>(resources);
        this.objects = new HashMap<>(objects);
    }

    @Override
    public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
        return (playerBoard, points) -> {
            return resources
              .entrySet()
              .stream()
              .map(
                (resource) -> {
                  return playerBoard.getResources().get(resource.getKey()) / resource.getValue();
                }
              ).reduce(Integer::min).orElse(0) * points
              + (objects
                .entrySet()
                .stream()
                .map(
                  (object) -> {
                      return playerBoard.getObjects().get(object.getKey()) / object.getValue();
                  }
                ).reduce(Integer::min).orElse(0) * points);
        };
    }

}
