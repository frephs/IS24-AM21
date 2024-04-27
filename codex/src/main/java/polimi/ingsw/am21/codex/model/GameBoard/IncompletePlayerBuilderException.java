package polimi.ingsw.am21.codex.model.GameBoard;

import polimi.ingsw.am21.codex.model.Cards.PlayableSide;
import polimi.ingsw.am21.codex.model.Player;

import java.util.ArrayList;
import java.util.List;

public class IncompletePlayerBuilderException extends RuntimeException {
  public IncompletePlayerBuilderException(String message) {
    super(message);
  }

  /**
   * @
   */
  public static void checkPlayerBuilder(Player.PlayerBuilder playerBuilder) {
    List<String> missingParams = new ArrayList<>();
    if (playerBuilder.getNickname().isEmpty())
      missingParams.add("nickname");
    if (playerBuilder.getTokenColor().isEmpty())
      missingParams.add("tokenColor");
    if (playerBuilder.getStarterCard().isEmpty())
      missingParams.add("starterCard");
    else if (playerBuilder.getStarterCard().get().getPlayedSideType().isEmpty())
      missingParams.add("starterCardSide");

    if (!missingParams.isEmpty())
      throw new IncompletePlayerBuilderException("Incomplete PlayerBuilder: " +
        "missing " + missingParams.toString());
  }
}
