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
  public ChatMessage(
    String sender,
    String recipient,
    String content,
    Date timestamp
  ) {
    this.recipient = recipient;
    this.sender = sender;
    this.content = content;
    this.timestamp = timestamp;
  }

  public ChatMessage(String sender, String recipient, String content) {
    this(recipient, sender, content, new Date());
  }

  /**
   * Message for all the players
   */
  public ChatMessage(String sender, String content) {
    this(sender, null, content);
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
