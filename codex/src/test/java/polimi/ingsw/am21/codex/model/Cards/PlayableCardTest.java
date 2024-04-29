package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayableCardTest {

  PlayableCard card;

  @BeforeEach
  void setup() {
    this.card = new PlayableCard(123, new ResourceCardFrontSide(456), null);
  }

  @Test
  void getKingdom() {
    assertInstanceOf(Optional.class, card.getKingdom());

    assertTrue(card.getKingdom().isEmpty());

    assertEquals(
      ResourceType.ANIMAL,
      new PlayableCard(123, null, null, ResourceType.ANIMAL)
        .getKingdom()
        .orElse(null)
    );
  }

  @Test
  void getPlayedSideType() {
    assertInstanceOf(Optional.class, card.getPlayedSideType());

    assertTrue(card.getPlayedSideType().isEmpty());

    card.setPlayedSideType(CardSideType.FRONT);
    assertEquals(CardSideType.FRONT, card.getPlayedSideType().orElse(null));
  }

  @Test
  void getPlayedSide() {
    assertInstanceOf(Optional.class, card.getPlayedSide());

    assertTrue(card.getPlayedSide().isEmpty());

    PlayableFrontSide front = new ResourceCardFrontSide(123);
    PlayableBackSide back = new PlayableBackSide(List.of(ResourceType.ANIMAL));
    PlayableCard playedCard = new PlayableCard(123, front, back);

    assertTrue(playedCard.getPlayedSide().isEmpty());

    playedCard.setPlayedSideType(CardSideType.FRONT);
    assertEquals(front, playedCard.getPlayedSide().orElse(null));

    playedCard.setPlayedSideType(CardSideType.BACK);
    assertEquals(back, playedCard.getPlayedSide().orElse(null));
  }

  @Test
  void setPlayedSideType() {
    card.setPlayedSideType(CardSideType.FRONT);
    assertEquals(CardSideType.FRONT, card.getPlayedSideType().orElse(null));
  }

  @Test
  void getCoveredCorners() {
    assertEquals(0, card.getCoveredCorners());
  }

  @Test
  void setCoveredCorners() {
    card.setCoveredCorners(123);
    assertEquals(123, card.getCoveredCorners());
  }

  @Test
  void getEvaluator() {
    assertEquals(0, card.getEvaluator().apply(null));
    card.setPlayedSideType(CardSideType.FRONT);
    assertEquals(456, card.getEvaluator().apply(null));
  }
}
