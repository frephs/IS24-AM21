package polimi.ingsw.am21.codex.view.GUI.utils;

import java.util.function.Supplier;
import javafx.geometry.HPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class GridCell extends AnchorPane {

  private GridCellStatus status = GridCellStatus.AVAILABLE;
  public static final int TARGET_WIDTH = 150;
  public static final int TARGET_HEIGHT = 100;
  public static final int CELL_WIDTH = (int) Math.round(
    TARGET_WIDTH * (1 - (double) 220 / 993)
  );
  public static final int CELL_HEIGHT = (int) Math.round(
    TARGET_HEIGHT * (1 - (double) 270 / 662)
  );

  /**
   * @param onClick A function to be executed when the cell is clicked (fires only
   *                when the cell is active)
   * @param getPlacementActive A function that returns true if the user is allowed
   *                           to place a card in the cell
   */
  public GridCell(Runnable onClick, Supplier<Boolean> getPlacementActive) {
    super();
    // Set the cell dimensions
    // Original card size is 993x662 px, and each corner is ~220x270 px
    setWidth(CELL_WIDTH);
    setHeight(CELL_HEIGHT);

    // Set centering so that the overflow is centered as well
    GridPane.setHalignment(this, HPos.CENTER);

    // Set mouse hover handlers
    setOnMouseEntered(event -> {
      getStyleClass().clear();
      if (getPlacementActive.get()) {
        if (status == GridCellStatus.AVAILABLE) {
          getStyleClass().add("grid-cell-active-hover");
        } else if (status == GridCellStatus.FORBIDDEN) {
          getStyleClass().add("grid-cell-forbidden-hover");
        }
      }
    });
    setOnMouseExited(event -> getStyleClass().clear());

    // Handle clicks
    setOnMouseClicked(event -> {
      if (status == GridCellStatus.AVAILABLE && getPlacementActive.get()) {
        onClick.run();
      }
    });
  }

  public void setStatus(GridCellStatus status) {
    this.status = status;
  }

  /**
   * Loads a card in the cell and makes it inactive
   */
  public void placeCard(ImageView cardImage) {
    setStatus(GridCellStatus.INACTIVE);

    cardImage.setFitWidth(TARGET_WIDTH);
    cardImage.setFitHeight(TARGET_HEIGHT);

    getChildren().clear();
    getChildren().add(cardImage);
  }
}
