package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class CornerTest {

  @Test
  void isEmpty() {
    Corner<CornerContentType> corner = new Corner<>();
    assertTrue(corner.isEmpty());
  }

  @Test
  void getContent() {
    Corner<CornerContentType> corner = new Corner<>();
    assertEquals(corner.getContent(), Optional.empty());
  }

  @Test
  void cover() {
    Corner<CornerContentType> corner = new Corner<>();
    assertFalse(corner.isCovered());
    corner.cover();
    assertTrue(corner.isCovered());
  }
}
