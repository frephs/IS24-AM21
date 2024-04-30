package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;

public class CardPlaceMessage extends ViewUpdatingMessage {
  public int x;
  public int y;
  public CardSideType side;
  public int cardId;
}
