package polimi.ingsw.am21.codex.model.Cards;

import java.util.Optional;

public class Corner<T extends CornerContentType> {

  /**
   * If it exists, it's the content of the corner
   */
  private final Optional<T> content;
  /**
   * The indicator for the occupation of the corner
   */
  private boolean isCovered;

  public Corner() {
    content = Optional.empty();
    isCovered = false;
  }

  public Corner(T content) {
    this.content = Optional.of(content);
    isCovered = false;
  }

  public boolean isEmpty() {
    return content.isEmpty();
  }

  public Optional<T> getContent() {
    return content;
  }

  public boolean isCovered() {
    return isCovered;
  }

  /**
   * Cover the corner when another card is placed on
   */
  public void cover() {
    isCovered = true;
  }
}
