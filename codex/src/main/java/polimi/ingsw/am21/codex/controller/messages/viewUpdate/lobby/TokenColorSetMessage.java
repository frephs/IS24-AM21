package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class TokenColorSetMessage extends ViewUpdatingMessage {

  private final TokenColor color;

  public TokenColorSetMessage(TokenColor color) {
    super(MessageType.TOKEN_COLOR_SET);
    this.color = color;
  }

  public TokenColor getColor() {
    return color;
  }

  @Override
  public String toString() {
    return getType() + "{" + "color=" + color + '}';
  }
}
