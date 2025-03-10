package polimi.ingsw.am21.codex.model.Player;

import java.io.Serializable;
import polimi.ingsw.am21.codex.view.GUI.utils.GuiElement;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public enum TokenColor implements Colorable, Serializable, GuiElement {
  RED,
  BLUE,
  GREEN,
  YELLOW;

  //BLACK;

  @Override
  public Color getColor() {
    return switch (this) {
      case RED -> Color.RED;
      case BLUE -> Color.BLUE;
      case GREEN -> Color.GREEN;
      case YELLOW -> Color.YELLOW;
      // This is disabled as it's not a token that the player can pick
      //case BLACK -> Color.BLACK;
    };
  }

  /**
   * Gets a TokenColor from a string corresponding to the enum keys
   * @param color the string to convert
   */
  public static TokenColor fromString(String color) {
    try {
      return TokenColor.valueOf(color.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public String getImagePath() {
    return (
      "tokens/" +
      switch (this) {
        case RED -> "CODEX_pion_rouge.png";
        case BLUE -> "CODEX_pion_bleu.png";
        case GREEN -> "CODEX_pion_vert.png";
        case YELLOW -> "CODEX_pion_jaune.png";
        // case BLACK -> "CODEX_pion_noir.png";
      }
    );
  }
}
