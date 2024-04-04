package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.function.Function;

public class ObjectiveCard extends Card{
    public ObjectiveCard(int id){
        super(id);
    }

    @Override
    public Function<PlayerBoard, Integer> getEvaluator() {
        return null;
    }
}
