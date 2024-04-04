package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.HashMap;
import java.util.function.Function;

public class CountingObjective extends Objective{
    /**
     * The map of which resources and how many to complete the objective
     */
    private HashMap<ResourceType, Integer> resources;
    /**
     * The map of which object and how many to complete the objective
     */
    private HashMap<ObjectType, Integer> objects;

    public CountingObjective(HashMap<ResourceType, Integer> resources, HashMap<ObjectType, Integer> objects) {
        this.resources = new HashMap<>(resources);
        this.objects = new HashMap<>(objects);
    }

    @Override
    public Function<PlayerBoard, Integer> getEvaluator() {
        return ((playerBoard, ))
    }

}
