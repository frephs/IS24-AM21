package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

public class ResourceCardFrontSide extends PlayableFrontSide {
    final int points;

    public ResourceCardFrontSide(int points) {
        this.points = points;
    }

    @Override
    public int evaluate(PlayerBoard playerBoard) {
        return points;
    }
}
