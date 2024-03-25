package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayableBackSideTest {

    @Test
    void getPermanentResources() {
        List<ResourceType> resources = new ArrayList<>();
        resources.add(ResourceType.ANIMAL_KINGDOM);

        PlayableBackSide a = new PlayableBackSide(resources);
        // Test that the resources are copied correctly (1 element)
        assertEquals(a.getPermanentResources(), resources);

        resources.add(ResourceType.FUNGI_KINGDOM);
        // Test that the internal Set is independent of the given reference
        assertEquals(a.getPermanentResources().size(), resources.size()-1);

        PlayableBackSide b = new PlayableBackSide((resources));
        // Test that the resources are copied correctly (2 elements)
        assertEquals(b.getPermanentResources(), resources);

        resources.add(ResourceType.INSECT_KINGDOM);
        PlayableBackSide c = new PlayableBackSide((resources));
        // Test that the resources are copied correctly (3 elements)
        assertEquals(c.getPermanentResources(), resources);
    }

    @Test
    void evaluate() {
        List<ResourceType> resources = new ArrayList<>();
        resources.add(ResourceType.ANIMAL_KINGDOM);
        PlayableBackSide a = new PlayableBackSide(resources);

        resources.add(ResourceType.PLANT_KINGDOM);
        PlayableBackSide b = new PlayableBackSide(resources);

        PlayerBoard pb = new PlayerBoard(new ArrayList<>(), new PlayableCard(), new ObjectiveCard());

        // .equals() should always return 0
        assertEquals(a.evaluate(pb), 0);
        assertEquals(a.evaluate(pb), 0);
    }
}