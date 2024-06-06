package polimi.ingsw.am21.codex.model.Lobby.exceptions;

import java.util.List;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class NicknameAlreadyTakenException extends InvalidActionException {

  public NicknameAlreadyTakenException(String nickname) {
    super(InvalidActionCode.NICKNAME_ALREADY_TAKEN, List.of(nickname));
  }

  public String getNickname() {
    return this.getNotes().get(0);
  }

  public String getFullMessage() {
    return "Nickname " + this.getNickname() + " is already taken";
  }
}
