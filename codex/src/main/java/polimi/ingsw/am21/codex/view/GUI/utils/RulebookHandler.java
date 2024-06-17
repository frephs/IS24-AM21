package polimi.ingsw.am21.codex.view.GUI.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import polimi.ingsw.am21.codex.view.GUI.Gui;

public class RulebookHandler {

  private static Stage rulebookStage;
  private RulebookLayout rulebookLayout;

  public RulebookHandler(Stage rulebookStage) {
    RulebookHandler.rulebookStage = rulebookStage;
    RulebookHandler.rulebookStage.initStyle(StageStyle.TRANSPARENT);
  }

  public void loadRulebook() throws IOException {
    rulebookLayout = new RulebookLayout();
    rulebookLayout.loadRulebook();
    Platform.runLater(this::showRulebook);
  }

  private void showRulebook() {
    Scene rulebookScene = new Scene(rulebookLayout.getParent());
    rulebookScene.setFill(Color.TRANSPARENT);
    rulebookStage.setScene(rulebookScene);
    rulebookStage.setAlwaysOnTop(true);
    rulebookStage.show();
  }

  public static class RulebookLayout extends DraggableLayout {

    Parent rulebookLayout;

    @FXML
    Node rulebookContainer;

    @FXML
    private HBox pageContainer;

    @FXML
    private ImageView page;

    @FXML
    private final List<Image> pages = new ArrayList<>();

    private int currentPage = 0;

    public RulebookLayout() {}

    public void loadRulebook() throws IOException {
      FXMLLoader fxmlLoader = new FXMLLoader(
        Gui.class.getResource("RulebookHandler.fxml")
      );
      fxmlLoader.setController(this);
      rulebookLayout = fxmlLoader.load();
      loadRulebookDetails();
    }

    public void loadRulebookDetails() {
      for (int i = 1; i <= 12; i++) {
        pages.add(
          new Image(
            String.valueOf(
              Gui.class.getResource(
                  "pictures/rulebook/CODEX_Rulebook_EN-" +
                  String.format("%02d", i) +
                  ".png"
                )
            )
          )
        );
      }
      updatePageView();

      pageContainer.setOnScroll(event -> {
        if (event.getDeltaY() < 0) {
          handleNextPage();
        } else {
          handlePreviousPage();
        }
      });

      rulebookLayout
        .lookup("#next-page-button")
        .setOnMouseClicked(event -> handleNextPage());

      rulebookLayout
        .lookup("#previous-page-button")
        .setOnMouseClicked(event -> handlePreviousPage());

      rulebookLayout
        .lookup("#close-rulebook-button")
        .setOnMouseClicked(event -> closeRulebook());
    }

    @FXML
    private void handleNextPage() {
      if (currentPage < pages.size() - 1) {
        currentPage++;
        updatePageView();
      }
    }

    @FXML
    private void handlePreviousPage() {
      if (currentPage > 0) {
        currentPage--;
        updatePageView();
      }
    }

    @FXML
    public void closeRulebook() {
      rulebookStage.hide();
    }

    private void updatePageView() {
      page.setImage(pages.get(currentPage));
    }

    public Parent getParent() {
      return rulebookLayout;
    }

    @Override
    public void setDraggable(Stage stage) {
      setDraggable(rulebookStage, new VBox(rulebookContainer));
    }
  }
}
