package polimi.ingsw.am21.codex.model.Lobby.exceptions;

import java.util.ArrayList;
import java.util.List;
import polimi.ingsw.am21.codex.model.Player.Player;

public class IncompletePlayerBuilderException extends Exception {

  public IncompletePlayerBuilderException(String message) {
    super(message);
  }

  /**
   * @param playerBuilder the current player builder
   */
  public static void checkPlayerBuilder(Player.PlayerBuilder playerBuilder)
    throws IncompletePlayerBuilderException {
    List<String> missingParams = new ArrayList<>();
    List<String> invalidParams = new ArrayList<>();
    if (playerBuilder.getNickname().isEmpty()) missingParams.add("nickname");
    if (playerBuilder.getTokenColor().isEmpty()) missingParams.add(
      "tokenColor"
    );
    if (
      playerBuilder.getStarterCard().getPlayedSideType().isEmpty()
    ) missingParams.add("starterCardSide");
    if (playerBuilder.getHand().isEmpty()) {
      missingParams.add("hand");
    } else if (playerBuilder.getHand().get().size() != 3) {
      invalidParams.add(
        "hand cards (expected 3 cards but found " +
        playerBuilder.getHand().get().size() +
        ")"
      );
    }

    if (playerBuilder.getObjectiveCard().isEmpty()) {
      missingParams.add("objectiveCard");
    }

    if (!missingParams.isEmpty() || !invalidParams.isEmpty()) {
      String missingPramsStr = !missingParams.isEmpty()
        ? "\nmissing: " + String.join(",", missingParams)
        : "";
      String invalidPramsStr = !invalidParams.isEmpty()
        ? "\ninvalid: " + String.join(",", invalidParams)
        : "";
      throw new IncompletePlayerBuilderException(
        "Incomplete/wrong " +
        "PlayerBuilder:" +
        missingPramsStr +
        invalidPramsStr
      );
    }
  }
}
