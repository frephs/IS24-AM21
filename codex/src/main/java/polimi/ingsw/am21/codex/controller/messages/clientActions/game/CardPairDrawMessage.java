package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class CardPairDrawMessage extends DeckDrawCardMessage {

  public boolean first;

  public CardPairDrawMessage(String gameId, DrawingDeckType deck) {
    super(MessageType.CARD_PAIR_DRAW, gameId, deck);
  }
}
