package polimi.ingsw.am21.codex.controller.messages.viewUpdate;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.FullUserGameContext;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class UserContextMessage extends ViewUpdatingMessage {

  private final FullUserGameContext context;

  public UserContextMessage(FullUserGameContext context) {
    super(MessageType.USER_CONTEXT);
    this.context = context;
  }

  public FullUserGameContext getContext() {
    return context;
  }

  @Override
  public String toString() {
    return (getType() + "{" + "context=" + context.toString() + '}');
  }
}
