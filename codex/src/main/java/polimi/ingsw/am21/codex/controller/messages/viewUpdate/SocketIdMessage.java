package polimi.ingsw.am21.codex.controller.messages.viewUpdate;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class SocketIdMessage extends ViewUpdatingMessage {

  private final UUID socketId;

  public SocketIdMessage(UUID socketId) {
    super(MessageType.SOCKET_ID);
    this.socketId = socketId;
  }

  public UUID getSocketId() {
    return socketId;
  }

  @Override
  public String toString() {
    return "SocketIdMessage{" + "socketId='" + socketId + '\'' + '}';
  }
}
