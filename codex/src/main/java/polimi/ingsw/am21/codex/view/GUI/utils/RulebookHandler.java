package polimi.ingsw.am21.codex.view.GUI.utils;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class RulebookHandler {

  @FXML
  private HBox pageContainer;

  @FXML
  private ImageView page;

  private final List<Image> pages;
  private int currentPage = 0;

  public RulebookHandler() {
    //pages = List.of(new Image("codex/src/main/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-1-1.png"), new Image("codex/src/main/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-1-2.png"), new Image("codex/src/main/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-3-1.png"), new Image("codex/src/main/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-4-1.png"), new Image("codex/src/main/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-5-1.png"), new Image("codex/src/main/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-6-1.png"), new Image("codex/src/main/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-7-1.png"));
    /*pages = List.of(
        new Image("/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-1-1.png"),
        new Image("/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-1-2.png"),
        new Image("/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-3-1.png"),
        new Image("/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-4-1.png"),
        new Image("/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-5-1.png"),
        new Image("/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-6-1.png"),
        new Image("/resources/polimi/ingsw/am21/codex/view/GUI/pictures/rulebook/CODEX_Rulebook_EN-7-1.png")
      );
      page.setImage(pages.get(currentPage));*/
    // TODO fix this
    pages = List.of();
  }

  @FXML
  private void handleNextPage() {
    if (currentPage < pages.size() - 1) {
      currentPage++;
      updatePageView();
    } else {
      currentPage = 0;
      updatePageView();
    }
  }

  @FXML
  public void handlePreviousPage() {
    if (currentPage > 0) {
      currentPage--;
      updatePageView();
    } else {
      currentPage = pages.size() - 1;
      updatePageView();
    }
  }

  @FXML
  public void closeRulebook() {
    pageContainer.setVisible(false);
  }

  private void updatePageView() {
    page.setImage(pages.get(currentPage));
  }
}
