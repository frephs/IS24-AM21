package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;

public class SelectCardSideMessage extends ActionMessage {

  private final String lobbyId;
  private final CardSideType cardSideType;

  public SelectCardSideMessage(
    UUID connectionID,
    CardSideType cardSideType,
    String lobbyId
  ) {
    super(MessageType.SELECT_CARD_SIDE, connectionID);
    this.cardSideType = cardSideType;
    this.lobbyId = lobbyId;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  public CardSideType getCardSideType() {
    return cardSideType;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "lobbyId='" +
      lobbyId +
      '\'' +
      ", cardSideType=" +
      cardSideType +
      '}'
    );
  }
}
