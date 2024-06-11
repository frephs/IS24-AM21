package polimi.ingsw.am21.codex.view.GUI.utils;

import javafx.geometry.HPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class GridCell extends AnchorPane {

  private GridCellStatus status = GridCellStatus.AVAILABLE;
  public static int targetWidth = 150;
  public static int targetHeight = 100;
  public static int cellWidth = (int) Math.round(
    targetWidth * (1 - (double) 220 / 993)
  );
  public static int cellHeight = (int) Math.round(
    targetHeight * (1 - (double) 270 / 662)
  );

  /**
   * @param onClick A function to be executed when the cell is clicked (fires only
   *                when the cell is active)
   */
  public GridCell(Runnable onClick) {
    super();
    // Set the cell dimensions
    // Original card size is 993x662 px, and each corner is ~220x270 px
    setWidth(cellWidth);
    setHeight(cellHeight);

    // Set centering so that the overflow is centered as well
    GridPane.setHalignment(this, HPos.CENTER);

    // Set mouse hover handlers
    setOnMouseEntered(event -> {
      getStyleClass().clear();
      if (status == GridCellStatus.AVAILABLE) {
        getStyleClass().add("grid-cell-active-hover");
      } else if (status == GridCellStatus.FORBIDDEN) {
        getStyleClass().add("grid-cell-forbidden-hover");
      }
    });
    setOnMouseExited(event -> getStyleClass().clear());

    // Handle clicks
    setOnMouseClicked(event -> {
      if (status == GridCellStatus.AVAILABLE) {
        onClick.run();
      }
    });
  }

  public void setStatus(GridCellStatus status) {
    this.status = status;
  }

  public void placeCard(ImageView cardImage) {
    setStatus(GridCellStatus.INACTIVE);

    cardImage.setFitWidth(targetWidth);
    cardImage.setFitHeight(targetHeight);

    getChildren().clear();
    getChildren().add(cardImage);
  }
}
