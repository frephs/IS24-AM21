package polimi.ingsw.am21.codex.model.Player;

import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public enum TokenColor implements Colorable {
  RED,
  BLUE,
  GREEN,
  YELLOW,
  BLACK;

  @Override
  public Color getColor() {
    return switch (this) {
      case RED -> Color.RED;
      case BLUE -> Color.BLUE;
      case GREEN -> Color.GREEN;
      case YELLOW -> Color.YELLOW;
      case BLACK -> Color.BLACK;
    };
  }
}
