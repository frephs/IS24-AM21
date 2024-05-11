package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public interface CornerContentType extends Colorable {
  // visitor pattern
  public default void acceptVisitor(CornerContentVisitor visitor) {}

  public void acceptVisitor(CornerContentVisitor visitor, int arg);

  Color getColor();
}
