package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class SetTokenColorMessage extends ActionMessage {

  private final TokenColor color;
  private final String lobbyId;

  public SetTokenColorMessage(
    UUID connectionID,
    TokenColor color,
    String lobbyId
  ) {
    super(MessageType.SET_TOKEN_COLOR, connectionID);
    this.color = color;
    this.lobbyId = lobbyId;
  }

  public TokenColor getColor() {
    return color;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  @Override
  public String toString() {
    return (
      getType() + "{" + "color=" + color + ", lobbyId='" + lobbyId + '\'' + '}'
    );
  }
}
