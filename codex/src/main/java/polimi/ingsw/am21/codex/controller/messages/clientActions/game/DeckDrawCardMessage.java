package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class DeckDrawCardMessage extends ActionMessage {

  public DrawingDeckType deck;
}
