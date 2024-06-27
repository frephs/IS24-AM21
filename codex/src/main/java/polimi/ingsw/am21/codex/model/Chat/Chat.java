package polimi.ingsw.am21.codex.model.Chat;

import java.util.LinkedList;

public class Chat {

  /**
   * The list of messages sent by the client
   */
  private final LinkedList<ChatMessage> sentMessages;

  public Chat() {
    sentMessages = new LinkedList<>();
  }

  public void postMessage(ChatMessage message) {
    sentMessages.push(message);
  }
}
