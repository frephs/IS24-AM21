package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;

public class PlaceCardMessage extends ActionMessage {

  public int x;
  public int y;
  public int handIndex;
  public CardSideType side;
}
