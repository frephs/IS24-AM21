package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.List;
import java.util.function.BiFunction;

/** TODO: NOT ACTUALLY IMPLEMENTED, FIX IN MERGE */
public abstract class PlayableSide {
    /**
     * The list of corners on the card side
     */
    List<Corner<ResourceType>> corners;

    public List<Corner<ResourceType>> getCorners() {
        return corners;
    }

    public void setCorner();

    /**
     * Generates a function that should be called to get the points that should be
     * attributed to a player when they place a card on this side. The returned
     * function requires a PlayerBoard an Integer representing the number of corners
     * the card is covering.
     */
    public abstract BiFunction<PlayerBoard, Integer, Integer> getEvaluator();
}
