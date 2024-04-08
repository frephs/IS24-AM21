package polimi.ingsw.am21.codex.model.Cards;

public enum CornerPosition {
    TOP_LEFT(0),
    BOTTOM_LEFT(1),
    TOP_RIGHT(2),
    BOTTOM_RIGHT(3);

    private final int index;

    CornerPosition(int index) {
        this.index = index;
    }

    public CornerPosition getOppositeCornerPosition() {
        return values()[(this.ordinal() + 2) % 4];
    }


}
