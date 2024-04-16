package polimi.ingsw.am21.codex.model.Cards;

public class MissingParametersException extends IllegalStateException {
    MissingParametersException(String missingAttribute) {
        super("Attribute " + missingAttribute + "is supposed to be set first");
    }
}
