package polimi.ingsw.am21.codex.controller.messages.viewUpdate;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerConnectionChangedMessage extends ViewUpdatingMessage {

  private final UUID connectionID;
  private final String nickname;
  private final GameController.UserGameContext.ConnectionStatus status;

  public PlayerConnectionChangedMessage(
    UUID connectionID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    super(MessageType.PLAYER_CONNECTION_CHANGED);
    this.connectionID = connectionID;
    this.nickname = nickname;
    this.status = status;
  }

  public UUID getConnectionID() {
    return connectionID;
  }

  public String getNickname() {
    return nickname;
  }

  public GameController.UserGameContext.ConnectionStatus getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "connectionID=" +
      connectionID +
      ", nickname='" +
      nickname +
      "', status=" +
      status +
      '}'
    );
  }
}
