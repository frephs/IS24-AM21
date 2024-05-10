package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;

public class DeckDrawCardMessage extends ActionMessage {

  private final String gameId;
  private final DrawingDeckType deck;

  public DeckDrawCardMessage(String gameId, DrawingDeckType deck) {
    this(MessageType.DECK_DRAW_CARD, gameId, deck);
  }

  protected DeckDrawCardMessage(
    MessageType type,
    String gameId,
    DrawingDeckType deck
  ) {
    super(type);
    this.gameId = gameId;
    this.deck = deck;
  }

  public String getGameId() {
    return gameId;
  }

  public DrawingDeckType getDeck() {
    return deck;
  }
}
