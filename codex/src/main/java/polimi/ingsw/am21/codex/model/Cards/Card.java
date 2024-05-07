package polimi.ingsw.am21.codex.model.Cards;

import java.util.function.Function;
import polimi.ingsw.am21.codex.cli.CliPrintable;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public abstract class Card implements CliPrintable {

  /**
   * A unique identifier for the card
   */
  private final int id;

  /**
   * @param id A unique identifier for the card
   */
  public Card(int id) {
    this.id = id;
  }

  /**
   * @return The unique identifier of the card
   */
  public int getId() {
    return id;
  }

  /**
   * Generates a function that, when called by passing the player board,
   * returns the points of the card.
   */
  public abstract Function<PlayerBoard, Integer> getEvaluator();
}
