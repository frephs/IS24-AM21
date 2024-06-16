package polimi.ingsw.am21.codex.view.GUI.utils;

public interface GuiElement {
  /**
   * Gets the path of the image representing the element, relative to the
   * base path. Does not need a leading slash.
   */
  String getImagePath();

  /**
   * Gets the base path for all images
   */
  static String getBasePath() {
    return "pictures/";
  }
}
