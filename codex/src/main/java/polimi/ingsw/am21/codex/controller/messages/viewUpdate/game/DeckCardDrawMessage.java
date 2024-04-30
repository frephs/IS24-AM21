package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class DeckCardDrawMessage extends ViewUpdatingMessage {
  public DrawingDeckType deck;
  public int cardId;
}
