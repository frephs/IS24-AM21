package polimi.ingsw.am21.codex.model.Cards.Playable;

import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

import java.util.function.BiFunction;

public class StarterCardFrontSide extends PlayableFrontSide {
    public StarterCardFrontSide() {}

    @Override
    public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
        return ((playerBoard, integer) -> 0);
    }
}
