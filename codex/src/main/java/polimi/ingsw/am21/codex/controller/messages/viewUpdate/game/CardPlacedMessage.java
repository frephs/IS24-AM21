package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;

public class CardPlacedMessage extends ViewUpdatingMessage {

  public int x;
  public int y;
  public CardSideType side;
  public int cardId;

  public CardPlacedMessage() {
    super(MessageType.CARD_PLACED);
  }
}
