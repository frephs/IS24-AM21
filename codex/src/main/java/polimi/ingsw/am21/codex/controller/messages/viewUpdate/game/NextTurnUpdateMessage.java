package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import java.util.Set;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class NextTurnUpdateMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final String nickname;
  private final Integer playerIndex;

  private final DrawingCardSource cardSource;
  private final DrawingDeckType deck;
  private final Boolean isLastRound;
  private final Integer drawnCardId;
  private final Integer newPairCardId;
  private final Set<Position> availableSpots;
  private final Set<Position> forbiddenSpots;

  public NextTurnUpdateMessage(
    String gameId,
    String nickname,
    Integer playerIndex,
    DrawingCardSource cardSource,
    DrawingDeckType deck,
    Integer cardId,
    Integer newPairCardId,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
  ) {
    this(
      MessageType.NEXT_TURN_UPDATE,
      gameId,
      nickname,
      playerIndex,
      false,
      cardSource,
      deck,
      cardId,
      newPairCardId,
      availableSpots,
      forbiddenSpots
    );
  }

  public NextTurnUpdateMessage(
    String gameId,
    String nickname,
    Integer playerIndex,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
  ) {
    this(
      MessageType.NEXT_TURN_UPDATE,
      gameId,
      nickname,
      playerIndex,
      true,
      null,
      null,
      null,
      null,
      availableSpots,
      forbiddenSpots
    );
  }

  /**
   * @param nickname The nickname of the player that has just drawn a card
   */
  protected NextTurnUpdateMessage(
    MessageType type,
    String gameId,
    String nickname,
    Integer playerIndex,
    Boolean isLastRound,
    DrawingCardSource cardSource,
    DrawingDeckType deck,
    Integer drawnCardId,
    Integer newPairCardId,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
  ) {
    super(type);
    this.gameId = gameId;
    this.nickname = nickname;
    this.playerIndex = playerIndex;
    this.cardSource = cardSource;
    this.deck = deck;
    this.isLastRound = isLastRound;
    this.drawnCardId = drawnCardId;
    this.newPairCardId = newPairCardId;
    this.availableSpots = availableSpots;
    this.forbiddenSpots = forbiddenSpots;
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

  public Set<Position> getAvailableSpots() {
    return availableSpots;
  }

  public Set<Position> getForbiddenSpots() {
    return forbiddenSpots;
  }

  public Integer getPlayerIndex() {
    return playerIndex;
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
