package polimi.ingsw.am21.codex.model.Cards;

import java.util.Optional;

public class Corner<T> {
    Optional<T> contentType;
    CornerEnum cornerNumber;
    Optional<T> content;

    Optional<PlayedCard> linkedCard;

    public Corner(T content){
        this.content = Optional.of(content);
    }

    boolean isLinked() {
        return linkedCard.isPresent();
    }

    public Optional<T> getActualContent() {
        return contentType;
    }
    void setLinkedCard(PlayedCard card){
        this.linkedCard = Optional.of(card);
    }

    private Optional<PlayedCard> getLinkedCard() {
        return linkedCard;
    }


}
