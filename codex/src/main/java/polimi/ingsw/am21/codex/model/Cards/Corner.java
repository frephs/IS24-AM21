package polimi.ingsw.am21.codex.model.Cards;

import java.util.Optional;

public class Corner<T extends ResourceType> {
    CornerPosition cornerNumber;
    Optional<T> content;
}
