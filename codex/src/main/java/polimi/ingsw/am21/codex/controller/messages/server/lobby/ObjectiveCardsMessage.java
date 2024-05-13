package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class ObjectiveCardsMessage extends ResponseMessage {

  private final Pair<Integer, Integer> idPair;

  public ObjectiveCardsMessage(Pair<Integer, Integer> idPair) {
    super(MessageType.OBJECTIVE_CARDS);
    this.idPair = idPair;
  }

  public Pair<Integer, Integer> getIdPair() {
    return idPair;
  }
}
