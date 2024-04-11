package polimi.ingsw.am21.codex.model.GameBoard;

import polimi.ingsw.am21.codex.model.TokenColors;

public class TokenAlreadyTakenException extends RuntimeException {
    TokenColors tokenColor;

    public TokenAlreadyTakenException(TokenColors color) {
        super("The " + color.name() + " token is already taken");
        this.tokenColor = color;
    }

    public TokenColors getTokenColor() {
        return this.tokenColor;
    }
}
