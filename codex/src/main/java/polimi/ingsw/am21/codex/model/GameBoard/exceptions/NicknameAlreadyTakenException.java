<<<<<<<< HEAD:codex/src/main/java/polimi/ingsw/am21/codex/model/Lobby/NicknameAlreadyTakenException.java
package polimi.ingsw.am21.codex.model.Lobby;
========
package polimi.ingsw.am21.codex.model.GameBoard.exceptions;
>>>>>>>> ffbb53e (controller progress):codex/src/main/java/polimi/ingsw/am21/codex/model/GameBoard/exceptions/NicknameAlreadyTakenException.java

public class NicknameAlreadyTakenException extends RuntimeException {
  String nickname;

  public NicknameAlreadyTakenException(String nickname) {
    super("The nickname " + nickname + " is already taken");
    this.nickname = nickname;
  }

  public String getNickname() {
    return this.nickname;
  }
}