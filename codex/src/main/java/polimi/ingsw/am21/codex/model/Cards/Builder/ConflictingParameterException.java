package polimi.ingsw.am21.codex.model.Cards.Builder;

public class ConflictingParameterException extends IllegalStateException {

  public ConflictingParameterException(
    String paramName,
    String expectedValue,
    String currValue
  ) {
    super(
      "Parameter " +
      paramName +
      " is expected to be " +
      expectedValue +
      ", but is currently " +
      currValue
    );
  }
}
