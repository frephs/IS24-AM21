package polimi.ingsw.am21.codex.model.Chat;

import java.util.Optional;

public class Message {
  private final String sender;
  private String recipient;
  private final String content;

  /**
   * Message for dedicated player
   */
  public Message(String sender, String recipient, String content) {
    this.sender = sender;
    this.recipient = recipient;
    this.content = content;
  }

  /**
   * Message for all the player
   */
  public Message(String sender, String content) {
    this.sender = sender;
    this.content = content;
  }

  public String getContent() {
    return content;
  }
  public String getSender() {
    return sender;
  }

  public Optional<String> getRecipient() {
    return Optional.ofNullable(recipient);
  }
}
