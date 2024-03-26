package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardPairTest {
    static CardPair<Card> getExamplePair() {
        return new CardPair<>(new ConcreteCard(123), new ConcreteCard(456));
    }

    @Test
    void getFirst() {
        assertEquals(getExamplePair().getFirst().getId(), 123);
    }

    @Test
    void getSecond() {
        assertEquals(getExamplePair().getFirst().getId(), 456);
    }

    @Test
    void replaceFirst() {
        CardPair<Card> pair = getExamplePair();
        pair.replaceFirst(new ConcreteCard(789));
        assertEquals(pair.getFirst().getId(), 789);
    }

    @Test
    void replaceSecond() {
        CardPair<Card> pair = getExamplePair();
        pair.replaceSecond(new ConcreteCard(789));
        assertEquals(pair.getSecond().getId(), 789);
    }

    @Test
    void swap() {
        CardPair<Card> pair = getExamplePair();
        pair.swap();
        assertEquals(pair.getFirst().getId(), 123);
        assertEquals(pair.getSecond().getId(), 456);
    }
}