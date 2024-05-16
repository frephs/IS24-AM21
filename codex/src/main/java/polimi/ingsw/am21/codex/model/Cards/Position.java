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

  public Position computeAdjacentPosition(AdjacentPosition adjacentPosition) {
    return switch (adjacentPosition) {
      // isometric grid
      case CornerPosition.TOP_LEFT -> new Position(x - 1, y); // was (-1, +1)
      case CornerPosition.TOP_RIGHT -> new Position(x, y + 1); // was (+1, +1 )
      case CornerPosition.BOTTOM_LEFT -> new Position(x, y - 1); // was (-1, -1 )
      case CornerPosition.BOTTOM_RIGHT -> new Position(x + 1, y); // was (+1. -1 )
      case EdgePosition.TOP -> new Position(x - 1, y + 1);
      case EdgePosition.CENTER -> new Position(x, y);
      case EdgePosition.BOTTOM -> new Position(x + 1, y - 1);
      default -> throw new IllegalStateException(
        "Unexpected value: " + adjacentPosition
      );
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

  @Override
  public String toString() {
    return "Position{" + "x=" + x + ", y=" + y + '}';
  }
}
