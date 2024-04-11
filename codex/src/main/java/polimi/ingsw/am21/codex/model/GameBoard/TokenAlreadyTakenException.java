package polimi.ingsw.am21.codex.model.GameBoard;

import polimi.ingsw.am21.codex.model.TokenColor;

public class TokenAlreadyTakenException extends RuntimeException {
    TokenColor tokenColor;

    public TokenAlreadyTakenException(TokenColor color) {
        super("The " + color.name() + " token is already taken");
        this.tokenColor = color;
    }

    public TokenColor getTokenColor() {
        return this.tokenColor;
    }
}
