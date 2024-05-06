package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class CardPairDrawMessage extends DeckDrawCardMessage {

  public boolean first;

  public CardPairDrawMessage() {
    super(MessageType.CARD_PAIR_DRAW);
  }
}
