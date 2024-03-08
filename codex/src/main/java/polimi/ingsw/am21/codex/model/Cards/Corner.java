package polimi.ingsw.am21.codex.model.Cards;

public class Corner<T> {
    T content;

    PlayableCard linkedCard;

    void setLinkedCard(){

    }

    public SidedCard getLinkedCard() {
        return linkedCard;
    }

    public Corner(T content){
        this.content = content;
    }

}
