package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class NextTurnMessage extends GameActionMessage {

  private final DrawingCardSource cardSource;
  private final DrawingDeckType deck;
  private final Boolean isLastRound;

  public NextTurnMessage(
    String gameId,
    String nickname,
    DrawingCardSource cardSource,
    DrawingDeckType deck
  ) {
    this(
      MessageType.NEXT_TURN_LAST_ROUNDS,
      gameId,
      nickname,
      false,
      cardSource,
      deck
    );
  }

  public NextTurnMessage(String gameId, String nickname) {
    this(MessageType.NEXT_TURN_LAST_ROUNDS, gameId, nickname, true, null, null);
  }

  protected NextTurnMessage(
    MessageType type,
    String gameId,
    String nickname,
    Boolean isLastRound,
    DrawingCardSource cardSource,
    DrawingDeckType deck
  ) {
    super(type, gameId, nickname);
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
}
