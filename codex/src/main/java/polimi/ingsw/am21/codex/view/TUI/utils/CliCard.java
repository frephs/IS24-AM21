package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.HashMap;

public interface CliCard {
  String cardToString(Cli.Options options);

  /**
   * Converts the card to an ASCII representation
   * @return The rendered ASCII string
   */
  default String cardToAscii(Cli.Options options) {
    return cardToAscii(options, new HashMap<>());
  }

  /**
   * Converts the card to an ASCII representation
   * @param cardStringMap Maps the position of the item to the corresponding string to render inside the card
   * @return The rendered ASCII string
   */
  String cardToAscii(
    Cli.Options options,
    HashMap<Integer, String> cardStringMap
  );

  static String playableCardToAscii(
    Cli.Options options,
    HashMap<Integer, String> cardStringMap
  ) {
    return (
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
      "───┘ "
    );
  }
}
