package polimi.ingsw.am21.codex.controller.messages;

public abstract class ResponseMessage extends ConfirmMessage {

  public ResponseMessage(MessageType type) {
    super(type);
  }
}
