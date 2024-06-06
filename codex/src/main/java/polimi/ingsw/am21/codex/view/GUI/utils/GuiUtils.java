package polimi.ingsw.am21.codex.view.GUI.utils;

import polimi.ingsw.am21.codex.model.Cards.ResourceType;

public class GuiUtils {

  /**
   * Gets the CSS color class for a given resource type
   */
  public static String getColorClass(ResourceType type) {
    return switch (type) {
      case ANIMAL -> "color-animal";
      case PLANT -> "color-plant";
      case INSECT -> "color-insect";
      case FUNGI -> "color-fungi";
    };
  }
}
