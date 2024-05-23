package polimi.ingsw.am21.codex.controller.messages;

public abstract class ConfirmMessage extends Message {

  public ConfirmMessage(MessageType type) {
    super(type);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
