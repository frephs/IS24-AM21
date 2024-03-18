package polimi.ingsw.am21.codex.model.Cards;

import java.util.Arrays;

public enum ResourceTypes {
    PLANT_KINGDOM,
    ANIMAL_KINGDOM,
    FUNGI_KINGDOM,
    INSECT_KINGDOM;

    public static <T> boolean has(T enumObject){
        return Arrays.stream(ResourceTypes.values()).anyMatch(
                value -> value == enumObject
        );
    }
}
