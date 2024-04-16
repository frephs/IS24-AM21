package polimi.ingsw.am21.codex.model.Cards.Builder;

public class WrongCardTypeException extends IllegalStateException {
    public WrongCardTypeException(String expected, String actual) {
        super("Parameter is allowed on type " + expected + ", but current type is " + actual + ".");
    }
}
