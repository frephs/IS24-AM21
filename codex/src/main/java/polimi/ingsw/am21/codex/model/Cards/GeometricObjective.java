package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.ArrayList;
import java.util.function.Function;

public class GeometricObjective extends Objective{

    private ResourceType[][] geometry;

    public GeometricObjective(ResourceType[][] geometry) {
        this.geometry = geometry;
    }
    @Override
    public Function<PlayerBoard, Integer> getEvaluator() {
        //deve ritornare quanti punti
    }

}
