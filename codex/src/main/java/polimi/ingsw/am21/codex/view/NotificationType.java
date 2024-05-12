package polimi.ingsw.am21.codex.view;

import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public enum NotificationType implements Colorable {
  WARNING,
  ERROR,
  RESPONSE,
  CONFIRM;

  @Override
  public Color getColor() {
    return switch (this) {
      case WARNING -> Color.YELLOW;
      case ERROR -> Color.RED;
      case RESPONSE -> Color.BLUE;
      case CONFIRM -> Color.GREEN;
    };
  }
}
