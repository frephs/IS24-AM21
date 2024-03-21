package polimi.ingsw.am21.codex.model.Cards;

import java.util.Arrays;

public enum ObjectType {
    QUILL,
    INKWELL,
    MANUSCRIPT;

    public static <T> boolean has(T enumObject){
        return Arrays.stream(ObjectType.values()).anyMatch(
               value -> value == enumObject
        );
    }
}
