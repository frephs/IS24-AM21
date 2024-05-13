package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.GameActionMessage;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class NextTurnUpdateMessage extends GameActionMessage {

  private final DrawingCardSource cardSource;
  private final DrawingDeckType deck;
  private final Boolean isLastRound;
  private final Integer drawnCardId;

  public NextTurnUpdateMessage(
    String gameId,
    String nickname,
    DrawingCardSource cardSource,
    DrawingDeckType deck,
    Integer cardId
  ) {
    this(
      MessageType.NEXT_TURN_UPDATE,
      gameId,
      nickname,
      false,
      cardSource,
      deck,
      cardId
    );
  }

  public NextTurnUpdateMessage(String gameId, String nickname) {
    this(
      MessageType.NEXT_TURN_UPDATE,
      gameId,
      nickname,
      true,
      null,
      null,
      null
    );
  }

  /**
   * @param nickname The nickname of the player that has just drawn a card
   */
  protected NextTurnUpdateMessage(
    MessageType type,
    String gameId,
    String nickname,
    Boolean isLastRound,
    DrawingCardSource cardSource,
    DrawingDeckType deck,
    // TODO remove this and put it in its own message, so that it's not shared with every client
    Integer drawnCardId
  ) {
    super(type, gameId, nickname);
    this.cardSource = cardSource;
    this.deck = deck;
    this.isLastRound = isLastRound;
    this.drawnCardId = drawnCardId;
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

  public Integer getDrawnCardId() {
    return drawnCardId;
  }
}
