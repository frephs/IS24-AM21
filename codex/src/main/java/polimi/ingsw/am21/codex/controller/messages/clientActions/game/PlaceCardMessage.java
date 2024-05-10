package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;

public class PlaceCardMessage extends ActionMessage {

  private final String gameId;
  private final String playerNickname;
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
    super(MessageType.PLACE_CARD);
    this.gameId = gameId;
    this.playerNickname = playerNickname;
    this.playerHandCardNumber = playerHandCardNumber;
    this.side = side;
    this.position = position;
  }

  public String getGameId() {
    return gameId;
  }

  public String getPlayerNickname() {
    return playerNickname;
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
