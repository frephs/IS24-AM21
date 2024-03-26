package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.function.Function;

/** This is a utility class that implements a bare-bone card: it's meant to exist for tests only. */
class ConcreteCard extends Card {
    public ConcreteCard(int id) {
        super(id);
    }

    @Override
    public Function<PlayerBoard, Integer> getEvaluator() {
        return null;
    }
}