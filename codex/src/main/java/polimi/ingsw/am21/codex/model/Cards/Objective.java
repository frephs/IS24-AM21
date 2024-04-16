package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.function.BiFunction;

public abstract class Objective {
    abstract public BiFunction<PlayerBoard, Integer, Integer>  getEvaluator();
}
