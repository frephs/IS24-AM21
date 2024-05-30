package polimi.ingsw.am21.codex.controller.messages;

public abstract class ActionMessage extends ClientMessage {

  public ActionMessage(MessageType type) {
    super(type);
  }
}
