package polimi.ingsw.am21.codex.controller.messages;

public abstract class ViewUpdatingMessage extends Message {

  private int playerId;

  public ViewUpdatingMessage(MessageType type) {
    super(type);
  }
}
