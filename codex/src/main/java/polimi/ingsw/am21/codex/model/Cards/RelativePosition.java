package polimi.ingsw.am21.codex.model.Cards;

public class RelativePosition {
    final int x, y;

    RelativePosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    private RelativePosition computeLinkingPosition(CornerEnum linkedCorner){
        RelativePosition r;
        switch (linkedCorner){
            case UP_LEFT: r = new RelativePosition(x-1, y+1); break;
            case UP_RIGHT: r = new RelativePosition(x+1, y+1); break;
            case DOWN_LEFT: r = new RelativePosition(x-1, y-1); break;
            case DOWN_RIGHT: r = new RelativePosition(x+1, y-1); break;
        }
        return r;
    }

}
