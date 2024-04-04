package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.function.Function;

public abstract class Objective {
    //cosa fa?
    abstract public Function<PlayerBoard, Integer> getEvaluator();
}
