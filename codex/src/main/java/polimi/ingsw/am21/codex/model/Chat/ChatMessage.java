package polimi.ingsw.am21.codex.model.Chat;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class ChatMessage implements Serializable {

  private final String recipient;
  private final String content;
  private final String sender;
  private final Date timestamp;

  /**
   * Message for chosen player
   */
  public ChatMessage(String recipient, String sender, String content) {
    this.recipient = recipient;
    this.sender = sender;
    this.content = content;
    this.timestamp = new Date();
  }

  /**
   * Message for all the players
   */
  public ChatMessage(String sender, String content) {
    this.content = content;
    this.sender = sender;
    this.recipient = null;
    this.timestamp = new Date();
  }

  public ChatMessage(UUID sender, String content) {
    this(sender.toString(), content);
  }

  public String getContent() {
    return content;
  }

  public String getSender() {
    return sender;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public Optional<String> getRecipient() {
    return Optional.ofNullable(recipient);
  }
}
