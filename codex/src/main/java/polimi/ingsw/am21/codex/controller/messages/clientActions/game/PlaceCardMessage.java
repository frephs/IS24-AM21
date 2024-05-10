package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;

public class PlaceCardMessage extends GameActionMessage {

  private final int playerHandCardNumber;
  private final CardSideType side;
  private final Position position;

  public PlaceCardMessage(
    String gameId,
    String playerNickname,
    int playerHandCardNumber,
    CardSideType side,
    Position position
  ) {
    super(MessageType.PLACE_CARD, gameId, playerNickname);
    this.playerHandCardNumber = playerHandCardNumber;
    this.side = side;
    this.position = position;
  }

  public int getPlayerHandCardNumber() {
    return playerHandCardNumber;
  }

  public CardSideType getSide() {
    return side;
  }

  public Position getPosition() {
    return position;
  }
}
