package polimi.ingsw.am21.codex.cli;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public interface PrintableCard {
  String cardToString();

  static String cardToAscii(Map<Integer, String> cardStringMap) {
    return (
      "┌───────────┐ " +
      "\n" +
      "│ " +
      cardStringMap.getOrDefault(0, " ") +
      cardStringMap.getOrDefault(5, "       ") +
      cardStringMap.getOrDefault(1, " ") +
      " │ " +
      "\n" +
      "│     " +
      cardStringMap.getOrDefault(4, " ") +
      "     │ " +
      "\n" +
      "│ " +
      cardStringMap.getOrDefault(3, " ") +
      cardStringMap.getOrDefault(6, "       ") +
      cardStringMap.getOrDefault(2, " ") +
      " │ " +
      "\n" +
      "└───────────┘ "
    );
  }
}
