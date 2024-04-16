package polimi.ingsw.am21.codex.model.Cards;

public enum CardType {
  RESOURCE,
  STARTER,
  GOLD,
  OBJECTIVE;

  public static CardType fromString(String str) {
    return CardType.valueOf(str);
  }
}
