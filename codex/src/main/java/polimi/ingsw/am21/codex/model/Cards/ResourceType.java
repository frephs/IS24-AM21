package polimi.ingsw.am21.codex.model.Cards;

import java.util.Arrays;

public enum ResourceType implements CornerContentType {
    PLANT_KINGDOM,
    ANIMAL_KINGDOM,
    FUNGI_KINGDOM,
    INSECT_KINGDOM;

    public static boolean has(Object enumObject){
        return Arrays.stream(ResourceType.values()).anyMatch(
                value -> value == enumObject
        );
    }

    public static ResourceType fromString(String resourceTypeStr){
        return ResourceType.valueOf(resourceTypeStr);
    }
}
