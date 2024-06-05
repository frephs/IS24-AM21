package polimi.ingsw.am21.codex.view.GUI.utils;

public interface GuiElement {
  String getImagePath();

  static String getBasePath() {
    return "pictures/";
  }
}
