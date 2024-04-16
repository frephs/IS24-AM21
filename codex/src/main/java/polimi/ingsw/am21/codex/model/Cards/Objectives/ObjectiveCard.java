package polimi.ingsw.am21.codex.model.Cards.Objectives;

import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.function.Function;

public class ObjectiveCard extends Card {
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

    /**
     * Take the PlayerBoard of player
     * @return The points of the objective card
     */
    @Override
    public Function<PlayerBoard, Integer> getEvaluator() {
        return playerBoard -> {
            return objective.getEvaluator().apply(playerBoard, points);
        };
    }
}
