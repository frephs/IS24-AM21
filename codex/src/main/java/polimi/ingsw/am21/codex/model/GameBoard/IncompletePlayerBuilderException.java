package polimi.ingsw.am21.codex.model.GameBoard;

import polimi.ingsw.am21.codex.model.Player;

import java.util.ArrayList;
import java.util.List;

public class IncompletePlayerBuilderException extends RuntimeException {
    public IncompletePlayerBuilderException(String message) {
        super(message);
    }

    /**
     * @
     *
     */
    public static void checkPlayerBuilder(Player.PlayerBuilder playerBuilder) {
        List<String> missingParams = new ArrayList<>();
        if (playerBuilder.getNickname() == null)
            missingParams.add("nickname");
        if (playerBuilder.getTokenColor() == null)
            missingParams.add("tokenColor");

        throw new IncompletePlayerBuilderException("Incomplete PlayerBuilder: missing ");
    }
}
