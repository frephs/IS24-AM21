package polimi.ingsw.am21.codex.model.Cards;

public class Position {

  private final int x, y;

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Position() {
    this.x = 0;
    this.y = 0;
  }

  public Position computeAdjacentPosition(CornerPosition linkedCorner) {
    return switch (linkedCorner) {
      case TOP_LEFT -> new Position(x - 1, y + 1);
      case TOP_RIGHT -> new Position(x + 1, y + 1);
      case BOTTOM_LEFT -> new Position(x - 1, y - 1);
      case BOTTOM_RIGHT -> new Position(x + 1, y - 1);
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Position position = (Position) o;
    return x == position.x && y == position.y;
  }

  @Override
  public int hashCode() {
    return 0; // TODO actually return something
  }
}
