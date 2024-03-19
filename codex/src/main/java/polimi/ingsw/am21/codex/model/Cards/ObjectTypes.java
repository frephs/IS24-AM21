package polimi.ingsw.am21.codex.model.Cards;

import java.util.Arrays;

public enum ObjectTypes {
    QUILL,
    INKWELL,
    MANUSCRIPT;

    public static <T> boolean has(T enumObject){
        return Arrays.stream(ObjectTypes.values()).anyMatch(
               value -> value == enumObject
        );
    }
}
