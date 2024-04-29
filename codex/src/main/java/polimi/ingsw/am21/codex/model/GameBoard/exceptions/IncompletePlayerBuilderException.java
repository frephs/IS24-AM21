<<<<<<<< HEAD:codex/src/main/java/polimi/ingsw/am21/codex/model/Lobby/IncompletePlayerBuilderException.java
package polimi.ingsw.am21.codex.model.Lobby;

import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableSide;
import polimi.ingsw.am21.codex.model.Player.Player;
========
package polimi.ingsw.am21.codex.model.GameBoard.exceptions;

import polimi.ingsw.am21.codex.model.Player;
>>>>>>>> ffbb53e (controller progress):codex/src/main/java/polimi/ingsw/am21/codex/model/GameBoard/exceptions/IncompletePlayerBuilderException.java

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
    else if (playerBuilder.getStarterCard().getPlayedSideType().isEmpty())
      missingParams.add("starterCardSide");

    if (!missingParams.isEmpty())
      throw new IncompletePlayerBuilderException("Incomplete PlayerBuilder: " +
        "missing " + missingParams.toString());
  }
}
