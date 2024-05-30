package polimi.ingsw.am21.codex.model.Chat;

import java.util.LinkedList;

public class Chat {

  private final LinkedList<Message> sentMessage;

  public Chat() {
    sentMessage = new LinkedList<>();
  }

  public void postMessage(Message message) {
    sentMessage.push(message);
  }
}
