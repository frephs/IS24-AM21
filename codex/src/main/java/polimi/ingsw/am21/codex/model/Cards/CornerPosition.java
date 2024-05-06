package polimi.ingsw.am21.codex.model.Cards;

public enum CornerPosition implements AdjacentPosition {
  TOP_LEFT(0),
  TOP_RIGHT(1),
  BOTTOM_RIGHT(2),
  BOTTOM_LEFT(3);

  public final int index;

  CornerPosition(int index) {
    this.index = index;
  }

  public CornerPosition getOppositeCornerPosition() {
    return values()[(this.ordinal() + 2) % 4];
  }
}
