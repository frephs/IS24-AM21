package polimi.ingsw.am21.codex.model.Lobby.exceptions;

public class NicknameAlreadyTakenException extends Exception {

  String nickname;

  public NicknameAlreadyTakenException(String nickname) {
    super("The nickname " + nickname + " is already taken");
    this.nickname = nickname;
  }

  public String getNickname() {
    return this.nickname;
  }
}
