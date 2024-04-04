package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.function.Function;

public class ObjectiveCard extends Card{
    /**
     * The points that the objective can give
     */
    private int points;
    /**
     * The objective of the card
     */
    private Objective objective;
    public ObjectiveCard(int id, int points, Objective objective) {
        super(id);
        this.points = points;
        this.objective = objective;
    }

    @Override
    public Function<PlayerBoard, Integer> getEvaluator() {
        //deve ritornare quanti punti fa piazzandola?
    }
}
