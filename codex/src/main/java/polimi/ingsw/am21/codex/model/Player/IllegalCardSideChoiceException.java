package polimi.ingsw.am21.codex.model.Player;

public class IllegalCardSideChoiceException extends IllegalArgumentException {

  public IllegalCardSideChoiceException() {
    super("You do not currently satisfy this side's placing conditions");
  }
}
