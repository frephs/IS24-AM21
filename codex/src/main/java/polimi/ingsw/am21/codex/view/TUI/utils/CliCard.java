package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.Map;

public interface CliCard {
  String cardToString();

  /**
   * Converts the card to an ASCII representation
   * @return The rendered ASCII string
   */
  default String cardToAscii() {
    return cardToAscii(Map.of());
  }

  /**
   * Converts the card to an ASCII representation
   * @param cardStringMap Maps the position of the item to the corresponding string to render inside the card
   * @return The rendered ASCII string
   */
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
      cardStringMap.getOrDefault(4, "     ") +
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
}
