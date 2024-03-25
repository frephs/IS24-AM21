package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.List;

/** TODO: NOT ACTUALLY IMPLEMENTED, FIX IN MERGE */
public abstract class PlayableSide {
    List<Corner> corners;

    public List<Corner> getCorners();
    public void setCorner();

    public abstract int evaluate(PlayerBoard playerBoard);
}
