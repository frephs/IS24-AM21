package polimi.ingsw.am21.codex.model.GameBoard;

public enum DrawingDeckType {
  RESOURCE,
  GOLD;

  @Override
  public String toString() {
    return switch (this) {
      case RESOURCE -> "resource cards";
      case GOLD -> "gold cards";
    };
  }
}
