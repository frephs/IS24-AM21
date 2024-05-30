package polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class TokenColorAlreadyTakenMessage extends ErrorMessage {

  private final TokenColor token;

  public TokenColorAlreadyTakenMessage(TokenColor token) {
    super(MessageType.TOKEN_COLOR_ALREADY_TAKEN);
    this.token = token;
  }

  public TokenColor getToken() {
    return token;
  }

  @Override
  public String toString() {
    return getType().toString() + "{ token=" + token + " }";
  }
}
