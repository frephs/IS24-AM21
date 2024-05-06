package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class SetTokenColorMessage extends ActionMessage {

  public TokenColor color;

  public SetTokenColorMessage() {
    super(MessageType.SET_TOKEN_COLOR);
  }
}
