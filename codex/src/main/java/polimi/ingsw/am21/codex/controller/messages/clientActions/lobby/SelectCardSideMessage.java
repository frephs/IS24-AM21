package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;

public class SelectCardSideMessage extends ActionMessage {

  private final String lobbyId;
  private final CardSideType cardSideType;

  public SelectCardSideMessage(CardSideType cardSideType, String lobbyId) {
    super(MessageType.SELECT_CARD_SIDE);
    this.cardSideType = cardSideType;
    this.lobbyId = lobbyId;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  public CardSideType getCardSideType() {
    return cardSideType;
  }
}
