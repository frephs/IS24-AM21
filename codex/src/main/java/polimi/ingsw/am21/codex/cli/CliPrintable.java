package polimi.ingsw.am21.codex.cli;

import java.util.Map;

public interface CliPrintable {
  String cardToString();
  String cardToAscii(Map<Integer, String> cardStringMap);

  static String playableCardToAscii(Map<Integer, String> cardStringMap) {
    String cardString =
      "┌───" +
      (cardStringMap.containsKey(0) ? "┬" : "─") +
      "─────" +
      (cardStringMap.containsKey(1) ? "┬" : "─") +
      "───┐ " +
      "\n" +
      "│ " +
      cardStringMap.getOrDefault(0, " ") +
      " " +
      (cardStringMap.containsKey(0) ? "│" : " ") +
      cardStringMap.getOrDefault(5, "     ") +
      (cardStringMap.containsKey(1) ? "│" : " ") +
      " " +
      cardStringMap.getOrDefault(1, " ") +
      " │ " +
      "\n" +
      (cardStringMap.containsKey(0) || cardStringMap.containsKey(3)
          ? "├───"
          : "│   ") +
      (cardStringMap.containsKey(0) && cardStringMap.containsKey(3)
          ? "┤"
          : cardStringMap.containsKey(0) && !cardStringMap.containsKey(3)
            ? "┘"
            : !cardStringMap.containsKey(0) && cardStringMap.containsKey(3)
              ? "┐"
              : " ") +
      "  " +
      cardStringMap.getOrDefault(4, " ") +
      "  " +
      (cardStringMap.containsKey(1) && cardStringMap.containsKey(2)
          ? "├"
          : cardStringMap.containsKey(1) && !cardStringMap.containsKey(2)
            ? "└"
            : !cardStringMap.containsKey(1) && cardStringMap.containsKey(2)
              ? "┌"
              : " ") +
      (cardStringMap.containsKey(0) || cardStringMap.containsKey(3)
          ? "───┤ "
          : "   │ ") +
      "\n" +
      "│ " +
      cardStringMap.getOrDefault(3, " ") +
      " " +
      (cardStringMap.containsKey(3) ? "│" : " ") +
      cardStringMap.getOrDefault(6, "     ") +
      (cardStringMap.containsKey(2) ? "│" : " ") +
      " " +
      cardStringMap.getOrDefault(2, " ") +
      " │ " +
      "\n" +
      "└───" +
      (cardStringMap.containsKey(3) ? "┴" : "─") +
      "─────" +
      (cardStringMap.containsKey(2) ? "┴" : "─") +
      "───┘ ";

    return cardString;
  }
  //  public static void main(String[] args) {
  //    HashMap<Integer, String> cardStringMap = new HashMap<Integer, String>();
  //    for (int i = 0; i < 6; i++) {
  //      cardStringMap.put(i, String.valueOf(i));
  //    }
  //    cardToAscii(cardStringMap);
  //  }
}
