package polimi.ingsw.am21.codex.model.Cards;

import java.util.Arrays;
import polimi.ingsw.am21.codex.view.GUI.utils.GuiElement;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public enum ObjectType implements CornerContentType, Colorable, GuiElement {
  QUILL,
  INKWELL,
  MANUSCRIPT;

  /**
   * Determines whether the given value is part of this enum
   */
  public static boolean has(Object value) {
    return Arrays.stream(ObjectType.values()).anyMatch(
      objectType -> objectType == value
    );
  }

  public static ObjectType fromString(String str) {
    return ObjectType.valueOf(str);
  }

  public static Boolean isObjectType(String value) {
    return Arrays.stream(Arrays.stream(ObjectType.values()).toArray())
      .map(Object::toString)
      .anyMatch(objectType -> objectType.equals(value));
  }

  @Override
  public void acceptVisitor(CornerContentVisitor visitor) {
    CornerContentType.super.acceptVisitor(visitor);
  }

  @Override
  public void acceptVisitor(CornerContentVisitor visitor, int arg) {
    visitor.visit(this, arg);
  }

  @Override
  public Color getColor() {
    return Color.YELLOW;
  }

  @Override
  public String getImagePath() {
    return (
      "objects/" +
      switch (this) {
        case QUILL -> "pen.png";
        case INKWELL -> "ink.png";
        case MANUSCRIPT -> "manuscript.png";
      }
    );
  }
}
