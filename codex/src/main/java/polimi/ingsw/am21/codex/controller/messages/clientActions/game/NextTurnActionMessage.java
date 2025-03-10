package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class NextTurnActionMessage extends GameActionMessage {

  private final DrawingCardSource cardSource;
  private final DrawingDeckType deck;
  private final Boolean isLastRound;

  public NextTurnActionMessage(
    UUID connectionID,
    String gameId,
    String nickname,
    DrawingCardSource cardSource,
    DrawingDeckType deck
  ) {
    this(
      MessageType.NEXT_TURN_ACTION,
      connectionID,
      gameId,
      nickname,
      false,
      cardSource,
      deck
    );
  }

  public NextTurnActionMessage(
    UUID connectionID,
    String gameId,
    String nickname
  ) {
    this(
      MessageType.NEXT_TURN_ACTION,
      connectionID,
      gameId,
      nickname,
      true,
      null,
      null
    );
  }

  protected NextTurnActionMessage(
    MessageType type,
    UUID connectionID,
    String gameId,
    String nickname,
    Boolean isLastRound,
    DrawingCardSource cardSource,
    DrawingDeckType deck
  ) {
    super(type, connectionID, gameId, nickname);
    this.cardSource = cardSource;
    this.deck = deck;
    this.isLastRound = isLastRound;
  }

  public DrawingCardSource getCardSource() {
    return cardSource;
  }

  public DrawingDeckType getDeck() {
    return deck;
  }

  public Boolean isLastRound() {
    return isLastRound;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "cardSource=" +
      cardSource +
      ", deck=" +
      deck +
      ", isLastRound=" +
      isLastRound +
      ", gameId='" +
      getGameId() +
      ", nickname='" +
      getPlayerNickname() +
      '}'
    );
  }
}
