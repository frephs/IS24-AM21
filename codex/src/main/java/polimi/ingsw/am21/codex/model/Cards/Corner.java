package polimi.ingsw.am21.codex.model.Cards;

public class Corner<T> {
    T content;
    PlayedCard linkedCard;
    
    void setLinkedCard(PlayedCard card){
        this.linkedCard = card;
    }

    private PlayedCard getLinkedCard() {
        return linkedCard;
    }


    public Corner(T content){
        this.content = content;
    }

}
