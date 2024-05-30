package polimi.ingsw.am21.codex.controller.messages;

public abstract class RequestMessage extends ClientMessage {

  public RequestMessage(MessageType type) {
    super(type);
  }
}
