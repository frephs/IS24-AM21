package polimi.ingsw.am21.codex.model.Chat;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public class ChatMessage implements Serializable {

  private final String recipient;
  private final String content;
  private final String sender;

  /**
   * Message for chosen player
   */
  public ChatMessage(String recipient, String sender, String content) {
    this.recipient = recipient;
    this.sender = sender;
    this.content = content;
  }

  /**
   * Message for all the players
   */
  public ChatMessage(String sender, String content) {
    this.content = content;
    this.sender = sender;
    this.recipient = null;
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

  public Optional<String> getRecipient() {
    return Optional.ofNullable(recipient);
  }
}
