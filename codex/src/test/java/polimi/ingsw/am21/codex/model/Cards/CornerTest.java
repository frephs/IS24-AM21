package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CornerTest {

  @Test
  void isEmpty() {
    Corner corner = new Corner<>();
    assertEquals(corner.isEmpty(), true);
  }

  @Test
  void getContent() {
    Corner corner = new Corner<>();
    assertEquals(corner.getContent(), Optional.empty());
  }

  @Test
  void cover() {
    Corner corner = new Corner<>();
    assertEquals(corner.isCovered(), false);
    corner.cover();
    assertEquals(corner.isCovered(), true);
  }
}