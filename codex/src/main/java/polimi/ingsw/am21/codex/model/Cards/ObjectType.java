package polimi.ingsw.am21.codex.model.Cards;

import java.util.Arrays;
import polimi.ingsw.am21.codex.cli.Color;
import polimi.ingsw.am21.codex.cli.Colorable;

public enum ObjectType implements CornerContentType, Colorable {
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

  /*
   * -----------------
   * TUI METHODS
   * -----------------
   * */

  @Override
  public Color getColor() {
    return Color.YELLOW_BOLD;
  }
}
