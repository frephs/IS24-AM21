package polimi.ingsw.am21.codex.controller.messages.serverErrors;

import java.util.List;
import java.util.Optional;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException.InvalidActionCode;
import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class InvalidActionMessage extends ErrorMessage {

  private final InvalidActionCode code;
  private final List<String> notes;

  public InvalidActionMessage(InvalidActionCode code, List<String> notes) {
    super(MessageType.INVALID_ACTION);
    this.code = code;
    this.notes = notes;
  }

  public InvalidActionCode getCode() {
    return code;
  }

  public Optional<List<String>> getNotes() {
    return Optional.ofNullable(notes);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
