package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static polimi.ingsw.am21.codex.model.Cards.ObjectType.QUILL;

class CornerTest {

  @Test
  void isEmpty() {
    Corner corner = new Corner<>();
    assertEquals(corner.isEmpty(), true);
  }

  @Test
  void getContent() {
    Corner corner = new Corner<>(QUILL);
    assertEquals(corner.getContent(), QUILL);
  }

  @Test
  void cover() {
    Corner corner = new Corner<>();
    assertEquals(corner.isEmpty(), true);
    corner.cover();
    assertEquals(corner.isEmpty(), false);
  }
}