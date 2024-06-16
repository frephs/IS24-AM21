package polimi.ingsw.am21.codex.controller.messages.viewUpdate;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;

public class ChatMessageMessage extends ViewUpdatingMessage {

  private final String gameID;
  private final ChatMessage chatMessage;

  public ChatMessageMessage(String gameID, ChatMessage chatMessage) {
    super(MessageType.WINNING_PLAYER);
    this.chatMessage = chatMessage;
    this.gameID = gameID;
  }

  public ChatMessage getMessage() {
    return chatMessage;
  }

  public String getGameID() {
    return gameID;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameID='" +
      gameID +
      '\'' +
      ", chatMessage=" +
      chatMessage +
      '}'
    );
  }
}
