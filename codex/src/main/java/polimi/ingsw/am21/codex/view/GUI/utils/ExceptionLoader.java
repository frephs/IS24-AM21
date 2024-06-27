package polimi.ingsw.am21.codex.view.GUI.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import polimi.ingsw.am21.codex.view.GUI.Gui;

public class ExceptionLoader {

  private static Stage exceptionStage;

  public ExceptionLoader(Stage exceptionStage) {
    ExceptionLoader.exceptionStage = exceptionStage;
    ExceptionLoader.exceptionStage.initStyle(StageStyle.TRANSPARENT);
  }

  public void loadException(Exception e) throws IOException {
    ExceptionLayout exceptionLayout = new ExceptionLayout();
    exceptionLayout.loadException();
    Platform.runLater(() -> {
      exceptionLayout.loadExceptionDetails(e);
      showException(exceptionLayout);
    });
  }

  private void showException(ExceptionLayout exceptionLayout) {
    Scene exceptionScene = new Scene(exceptionLayout.getParent());
    exceptionScene.setFill(Color.TRANSPARENT);
    exceptionStage.setScene(exceptionScene);
    exceptionStage.show();
  }

  public static class ExceptionLayout extends DraggableLayout {

    Parent exceptionLayout;

    @FXML
    Node exceptionContainer;

    @FXML
    private VBox exceptionMessageVBox;

    @FXML
    private Text exceptionTitle;

    @FXML
    private Button exceptionCloseButton;

    public ExceptionLayout() {}

    public Parent getParent() {
      return exceptionLayout;
    }

    /**
     * Loads the exception layout
     */
    private void loadException() throws IOException {
      FXMLLoader fxmlLoader = new FXMLLoader(
        Gui.class.getResource("Exception.fxml")
      );
      fxmlLoader.setController(this);
      exceptionLayout = fxmlLoader.load();
    }

    /**
     * Populates the exception layout with the details from the given exception
     */
    public void loadExceptionDetails(Exception exception) {
      exceptionTitle.setText(exception.getClass().getSimpleName());
      exceptionCloseButton.setOnMouseClicked(
        (MouseEvent event) -> exceptionStage.close()
      );

      Text message = new Text(
        exception.getMessage() != null
          ? "Message: " + exception.getMessage()
          : ""
      );
      message.getStyleClass().add("exception-text");

      exceptionMessageVBox.getChildren().add(message);

      exceptionMessageVBox
        .getChildren()
        .addAll(
          exception.getStackTrace().length > 0
            ? Arrays.stream(exception.getStackTrace())
              .map(StackTraceElement::toString)
              .map(line -> {
                Text text = new Text(line);
                text.getStyleClass().add("exception-text");
                text.setWrappingWidth(exceptionMessageVBox.getWidth() - 40);
                return text;
              })
              .map(line -> {
                HBox hBox = new HBox(line);
                hBox.paddingProperty().setValue(new Insets(5, 0, 0, 0));
                return hBox;
              })
              .toList()
            : List.of(new Text("No stack trace provided"))
        );
    }

    @Override
    public void setDraggable(Stage exceptionStage) {
      setDraggable(exceptionStage, new VBox(exceptionContainer));
    }
  }
}
