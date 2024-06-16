package polimi.ingsw.am21.codex.view;

import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public enum NotificationType implements Colorable {
  WARNING,
  ERROR,
  RESPONSE,
  UPDATE,
  CONFIRM;

  @Override
  public Color getColor() {
    return switch (this) {
      case WARNING -> Color.YELLOW;
      case ERROR -> Color.RED;
      case RESPONSE -> Color.CYAN;
      case UPDATE -> Color.BLUE;
      case CONFIRM -> Color.GREEN;
    };
  }

  public String getStyleClass() {
    return switch (this) {
      case WARNING -> "warning";
      case ERROR -> "error";
      case RESPONSE -> "response";
      case UPDATE -> "update";
      case CONFIRM -> "confirm";
    };
  }
}
