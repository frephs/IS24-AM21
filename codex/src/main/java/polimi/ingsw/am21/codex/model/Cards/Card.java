package polimi.ingsw.am21.codex.model.Cards;

import java.util.function.Function;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.GUI.utils.GuiElement;
import polimi.ingsw.am21.codex.view.TUI.utils.CliCard;

public abstract class Card implements CliCard, GuiElement {

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

  @Override
  public String getImagePath() {
    return getImagePath(CardSideType.FRONT);
  }

  /**
   * Returns the path of the image of the card, given the side type
   * @param sideType The visible side of the card
   */
  public String getImagePath(CardSideType sideType) {
    return switch (sideType) {
      case FRONT -> "cards/front/" + String.format("%03d", id) + ".png";
      case BACK -> "cards/back/" + String.format("%03d", id) + ".png";
    };
  }
}
