package polimi.ingsw.am21.codex.model.Chat;

import java.util.LinkedList;

public class Chat {

  private final LinkedList<ChatMessage> sentMessages;

  public Chat() {
    sentMessages = new LinkedList<>();
  }

  public void postMessage(ChatMessage message) {
    sentMessages.push(message);
  }
}
