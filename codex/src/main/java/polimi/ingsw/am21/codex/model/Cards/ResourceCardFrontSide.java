package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.function.BiFunction;

public class ResourceCardFrontSide extends PlayableFrontSide {
    /**
     * The points the card should attribute to the player
     */
    final int points;

    /**
     * Constructor
     * @param points The points the card should attribute to the player
     */
    public ResourceCardFrontSide(int points) {
        this.points = points;
    }

    @Override
    public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
        return ((playerBoard, integer) -> points);
    }
}
