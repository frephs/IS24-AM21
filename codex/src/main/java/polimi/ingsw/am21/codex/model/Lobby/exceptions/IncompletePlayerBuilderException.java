package polimi.ingsw.am21.codex.model.Lobby.exceptions;

import polimi.ingsw.am21.codex.model.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class IncompletePlayerBuilderException extends RuntimeException {
  public IncompletePlayerBuilderException(String message) {
    super(message);
  }

  /**
   * @param playerBuilder the current player builder
   */
  public static void checkPlayerBuilder(Player.PlayerBuilder playerBuilder) {
    List<String> missingParams = new ArrayList<>();
    List<String> invalidParams = new ArrayList<>();
    if (playerBuilder.getNickname().isEmpty())
      missingParams.add("nickname");
    if (playerBuilder.getTokenColor().isEmpty())
      missingParams.add("tokenColor");
    if (playerBuilder.getStarterCard().getPlayedSideType().isEmpty())
      missingParams.add("starterCardSide");
    if (playerBuilder.getHand().isEmpty()) {
      missingParams.add("hand");
    } else if (playerBuilder.getHand().get().size() != 3) {
      invalidParams.add("hand cards");
    }

    if (!missingParams.isEmpty() || !invalidParams.isEmpty()) {
      String missingPramsStr = !missingParams.isEmpty() ?
        "\nmissing " + missingParams.toString() : "";
      String invalidPramsStr = !invalidParams.isEmpty() ?
        "\ninvalid" + invalidParams.toString() : "";
      throw new IncompletePlayerBuilderException("Incomplete/wrong " +
        "PlayerBuilder:" + missingPramsStr + invalidPramsStr
      );
    }
  }
}
