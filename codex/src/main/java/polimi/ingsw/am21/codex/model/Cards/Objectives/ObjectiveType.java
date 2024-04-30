package polimi.ingsw.am21.codex.model.Cards.Objectives;

public enum ObjectiveType {
  GEOMETRIC,
  COUNTING;

  public static ObjectiveType fromString(String key) {
    return valueOf(key);
  }
}
