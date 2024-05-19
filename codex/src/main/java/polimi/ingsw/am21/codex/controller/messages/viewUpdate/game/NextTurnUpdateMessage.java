package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class NextTurnUpdateMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final String nickname;

  private final DrawingCardSource cardSource;
  private final DrawingDeckType deck;
  private final Boolean isLastRound;
  private final Integer drawnCardId;
  private final Integer newPairCardId;

  public NextTurnUpdateMessage(
    String gameId,
    String nickname,
    DrawingCardSource cardSource,
    DrawingDeckType deck,
    Integer cardId,
    Integer newPairCardId
  ) {
    this(
      MessageType.NEXT_TURN_UPDATE,
      gameId,
      nickname,
      false,
      cardSource,
      deck,
      cardId,
      newPairCardId
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
    // TODO remove this and put it in its own message, so that it's not shared with every client?
    Integer drawnCardId,
    Integer newPairCardId
  ) {
    super(type);
    this.gameId = gameId;
    this.nickname = nickname;
    this.cardSource = cardSource;
    this.deck = deck;
    this.isLastRound = isLastRound;
    this.drawnCardId = drawnCardId;
    this.newPairCardId = newPairCardId;
  }

  public String getGameId() {
    return gameId;
  }

  public String getNickname() {
    return nickname;
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

  public Integer getNewPairCardId() {
    return newPairCardId;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId=" +
      gameId +
      ", nickname=" +
      nickname +
      "cardSource=" +
      cardSource +
      ", deck=" +
      deck +
      ", isLastRound=" +
      isLastRound +
      ", drawnCardId=" +
      drawnCardId +
      '}'
    );
  }
}
