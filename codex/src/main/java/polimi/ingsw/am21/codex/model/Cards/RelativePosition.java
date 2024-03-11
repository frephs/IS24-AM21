package polimi.ingsw.am21.codex.model.Cards;

public class RelativePosition {
    final int x, y;

    RelativePosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    RelativePosition(){
        this.x = 0;
        this.y = 0;
    }

    public RelativePosition computeLinkingPosition(CornerEnum linkedCorner){
        return switch (linkedCorner) {
            case UP_LEFT -> new RelativePosition(x - 1, y + 1);
            case UP_RIGHT -> new RelativePosition(x + 1, y + 1);
            case DOWN_LEFT -> new RelativePosition(x - 1, y - 1);
            case DOWN_RIGHT -> new RelativePosition(x + 1, y - 1);
        };
    }

}
