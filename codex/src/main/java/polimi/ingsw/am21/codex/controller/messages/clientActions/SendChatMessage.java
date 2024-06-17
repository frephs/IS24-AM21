package polimi.ingsw.am21.codex.controller.messages.clientActions;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;

public class SendChatMessage extends ActionMessage {

  private final ChatMessage message;

  public SendChatMessage(UUID connectionID, ChatMessage message) {
    super(MessageType.SEND_CHAT_MESSAGE, connectionID);
    this.message = message;
  }

  public ChatMessage getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "message='" +
      message +
      ", connectionID" +
      getConnectionID() +
      '}'
    );
  }
}
