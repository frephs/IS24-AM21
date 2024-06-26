package polimi.ingsw.am21.codex.view.GUI.utils;

import polimi.ingsw.am21.codex.model.Cards.Position;

public class GUIGridPosition {

  private final int row;
  private final int col;

  // This needs to be odd to avoid off centering
  public static int gridSize = 201;

  public GUIGridPosition(int row, int col) {
    this.row = row;
    this.col = col;
  }

  public GUIGridPosition(Position modelPosition) {
    this(
      gridSize / 2 + modelPosition.getX() - modelPosition.getY(),
      gridSize / 2 + modelPosition.getX() + modelPosition.getY()
    );
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  /**
   * Calculates the model position corresponding to this view position
   */
  public Position getModelPosition() {
    return new Position((row + col) / 2 - gridSize, (col - row) / 2);
  }
}
