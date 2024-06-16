package polimi.ingsw.am21.codex.view.GUI.utils;

import javafx.scene.Node;
import javafx.stage.Stage;

public abstract class DraggableLayout {

  private double xOffset = 0;
  private double yOffset = 0;

  public abstract void setDraggable(Stage stage);

  public void setDraggable(Stage stage, Node window) {
    window.setOnMousePressed(event -> {
      xOffset = event.getSceneX();
      yOffset = event.getSceneY();
    });

    window.setOnMouseDragged(event -> {
      stage.setX(event.getScreenX() - xOffset);
      stage.setY(event.getScreenY() - yOffset);
    });
  }
}
