package polimi.ingsw.am21.codex.view.TUI.utils;

import polimi.ingsw.am21.codex.model.Cards.Position;

public class CLIGridPosition {

  public static final int HEIGHT_OFFEST = 2;
  public static final int WIDTH_OFFSET = 10;

  private static final int startStringRow =
    (CliPlayerBoard.SECTION_ROWS / 2) * HEIGHT_OFFEST;
  private static final int startStringColumn =
    (CliPlayerBoard.SECTION_COLS / 2) * WIDTH_OFFSET;

  private final int row;
  private final int col;

  /**
   * @param modelPosition The position of the card in the model
   */
  public CLIGridPosition(Position modelPosition) {
    row = modelPosition.getX() - modelPosition.getY();
    col = modelPosition.getX() + modelPosition.getY();
  }

  /**
   * Gets the grid row, considering the visible section
   * @param verticalOffset The vertical offset of the grid (+1 shows the page under,
   *                       -1 shows the page above)
   */
  public int getTranslatedRow(int verticalOffset) {
    return row - CliPlayerBoard.SECTION_ROWS * verticalOffset;
  }

  /**
   * Gets the grid column, considering the visible section
   * @param horizontalOffset The horizontal offset of the grid (+1 shows the page
   *                         to the right, -1 shows the page to the left)
   */
  public int getTranslatedColumn(int horizontalOffset) {
    return col - CliPlayerBoard.SECTION_COLS * horizontalOffset;
  }

  /**
   * Gets the cursor row, considering the visible section
   * @param verticalOffset The vertical offset of the grid (+1 shows the page under,
   *                       -1 shows the page above)
   */
  public int getStringRow(int verticalOffset) {
    return startStringRow + (getTranslatedRow(verticalOffset)) * HEIGHT_OFFEST;
  }

  /**
   * Gets the cursor column, considering the visible section
   * PLEASE NOTE: This method does not account for modifiers offsets
   * @param horizontalOffset The horizontal offset of the grid (+1 shows the page
   *                         to the right, -1 shows the page to the left)
   */
  public int getStringColumn(int horizontalOffset) {
    return (
      startStringColumn + getTranslatedColumn(horizontalOffset) * WIDTH_OFFSET
    );
  }
}
