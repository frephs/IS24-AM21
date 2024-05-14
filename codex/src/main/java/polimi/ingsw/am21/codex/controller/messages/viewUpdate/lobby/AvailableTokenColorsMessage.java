package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.List;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class AvailableTokenColorsMessage extends ViewUpdatingMessage {

  private final List<TokenColor> colors;

  public AvailableTokenColorsMessage(List<TokenColor> colors) {
    super(MessageType.AVAILABLE_TOKEN_COLORS);
    this.colors = colors;
  }

  public List<TokenColor> getColors() {
    return colors;
  }
}
