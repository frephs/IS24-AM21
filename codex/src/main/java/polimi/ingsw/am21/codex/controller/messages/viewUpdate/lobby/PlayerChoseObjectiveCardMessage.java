package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerChoseObjectiveCardMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final UUID socketId;
  private final String nickname;

  public PlayerChoseObjectiveCardMessage(
    String gamedId,
    UUID socketId,
    String nickname
  ) {
    super(MessageType.PLAYER_CHOSE_OBJECTIVE);
    this.gameId = gamedId;
    this.socketId = socketId;
    this.nickname = nickname;
  }

  public String getGameId() {
    return gameId;
  }

  public UUID getSocketId() {
    return socketId;
  }

  public String getNickname() {
    return nickname;
  }

  @Override
  public String toString() {
    return (
      getType() + "{" + "gameId='" + gameId + "', socketId=" + socketId + '}'
    );
  }
}
