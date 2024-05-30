package polimi.ingsw.am21.codex.model.GameBoard;

import java.io.Serializable;

public enum DrawingDeckType implements Serializable {
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
