package polimi.ingsw.am21.codex.model.Cards;

import java.util.Arrays;

public enum ResourceType implements CornerContentType {
    PLANT,
    ANIMAL,
    FUNGI,
    INSECT;

    public static boolean has(Object value){
        return Arrays.stream(ResourceType.values()).anyMatch(
                resourceType -> resourceType == value
        );
    }

    public static ResourceType fromString(String resourceTypeStr){
        return ResourceType.valueOf(resourceTypeStr);
    }

    public static Boolean isResourceType(String value) {
        return Arrays.stream(Arrays.stream(ResourceType.values()).toArray())
          .map(Object::toString)
          .anyMatch(
            resourceType -> resourceType.equals(value)
          );
    }
}
