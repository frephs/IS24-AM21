package polimi.ingsw.am21.codex.model.Cards;

public enum PointConditionType {
  OBJECTS,
  CORNERS;

  public static PointConditionType fromString(String str) {
    return PointConditionType.valueOf(str);
  }
}
