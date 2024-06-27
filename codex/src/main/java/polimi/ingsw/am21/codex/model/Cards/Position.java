package polimi.ingsw.am21.codex.model.Cards;

import java.io.Serializable;

/**
 * Represents a position in the isometric grid
 * The isometric grid is a grid with the following properties:<br>
 * 1. The origin is in the center of the grid<br>
 * 2. The x-axis is oriented to the bottom-right part of the screen<br>
 * 3. The y-axis is oriented to the top-right part of the screen<br>
 */
public class Position implements Serializable {

  private final Integer x, y;

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Position() {
    this.x = 0;
    this.y = 0;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  /**
   * Calculates the position of the given relative offset
   */
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
    return x.equals(position.x) && y.equals(position.y);
  }

  @Override
  public int hashCode() {
    int result = Integer.hashCode(x);
    result = 31 * result + Integer.hashCode(y);
    return result;
  }

  @Override
  public String toString() {
    return "Position{" + "x=" + x + ", y=" + y + '}';
  }
}
