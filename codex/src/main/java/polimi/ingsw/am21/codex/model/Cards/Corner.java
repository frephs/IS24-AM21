package polimi.ingsw.am21.codex.model.Cards;

import java.util.Optional;

public class Corner<T extends CornerContentType> {
    /**
     * If it exists, it's the content of the corner
      */
  private Optional<T> content;
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
    return isCovered;
  }

  public Optional<T> getContent() {
      return content;
  }

    /**
     * Cover the corner when another card is placed on
     */
  public void cover() {
      isCovered = true;
  }
}
