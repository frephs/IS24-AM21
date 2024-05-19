package polimi.ingsw.am21.codex.controller.messages;

public class ConfirmMessage extends Message {

  public ConfirmMessage(MessageType type) {
    super(type);
  }

  public ConfirmMessage() {
    this(MessageType.CONFIRM);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
