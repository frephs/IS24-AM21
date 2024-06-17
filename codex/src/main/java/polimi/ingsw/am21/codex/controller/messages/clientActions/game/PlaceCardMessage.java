package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;

public class PlaceCardMessage extends GameActionMessage {

  private final int playerHandCardNumber;
  private final CardSideType side;
  private final Position position;

  public PlaceCardMessage(
    UUID connectionID,
    String gameId,
    String playerNickname,
    int playerHandCardNumber,
    CardSideType side,
    Position position
  ) {
    super(MessageType.PLACE_CARD, connectionID, gameId, playerNickname);
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

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "playerHandCardNumber=" +
      playerHandCardNumber +
      ", side=" +
      side +
      ", position=" +
      position +
      ", gameId='" +
      getGameId() +
      ", nickname='" +
      getPlayerNickname() +
      '}'
    );
  }
}
