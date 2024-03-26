package polimi.ingsw.am21.codex.model.Cards;

import java.util.Arrays;

public enum ObjectType implements CornerContentType {
    QUILL,
    INKWELL,
    MANUSCRIPT;

    /**
     * Determines whether the given value is part of this enum
     */
    public static boolean has(Object value) {
        return Arrays.stream(ObjectType.values()).anyMatch(
                objectType -> objectType == value
        );
    }
}
