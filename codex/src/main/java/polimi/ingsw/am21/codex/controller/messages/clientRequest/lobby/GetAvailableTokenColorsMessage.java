package polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.RequestMessage;

public class GetAvailableTokenColorsMessage extends RequestMessage {

  public GetAvailableTokenColorsMessage() {
    super(MessageType.GET_AVAILABLE_TOKEN_COLORS);
  }
}
