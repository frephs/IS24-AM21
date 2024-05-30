package polimi.ingsw.am21.codex.controller.messages.clientActions;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;

public class SendChatMessage extends ActionMessage {

  private final ChatMessage message;
  private final String gameId;

  public SendChatMessage(String gameId, ChatMessage message) {
    super(MessageType.SEND_CHAT_MESSAGE);
    this.message = message;
    this.gameId = gameId;
  }

  public ChatMessage getMessage() {
    return message;
  }

  public String getGameId() {
    return gameId;
  }

  @Override
  public String toString() {
    return (
      getType() + "{" + "message='" + message + ", gameId" + gameId + '}'
    );
  }
}
