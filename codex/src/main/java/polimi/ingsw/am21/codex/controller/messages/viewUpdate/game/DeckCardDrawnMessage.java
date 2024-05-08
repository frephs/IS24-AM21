package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class DeckCardDrawnMessage extends ViewUpdatingMessage {

  public DrawingDeckType deck;
  public int cardId;

  public DeckCardDrawnMessage() {
    super(MessageType.DECK_CARD_DRAWN);
  }
}
