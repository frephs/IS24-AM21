package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.function.Function;

public abstract class Card {
    /**
     * A unique identifier for the card
     */
    final int id;

    /**
     * @param id A unique identifier for the card
     */
    public Card(int id) {
        this.id = id;
    }

    /**
     * @return The unique identifier of the card
     */
    public int getId() {
        return id;
    }

    /**
     * Generates a function that, when called by passing the player board,
     * returns the points of the card.
     */
    abstract public Function<PlayerBoard, Integer> getEvaluator();
}
