package polimi.ingsw.am21.codex.model.Cards;

public class Position {
    final int x, y;

    public Position(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Position(){
        this.x = 0;
        this.y = 0;
    }

    public Position computeLinkingPosition(CornerPosition linkedCorner){
        return switch (linkedCorner) {
            case UP_LEFT -> new Position(x - 1, y + 1);
            case UP_RIGHT -> new Position(x + 1, y + 1);
            case DOWN_LEFT -> new Position(x - 1, y - 1);
            case DOWN_RIGHT -> new Position(x + 1, y - 1);
        };
    }

    public boolean equals(Position p) {
        return p.x == this.x && p.y == this.y;
    }

    @Override
    public int hashCode() {
        return 0; // TODO actually return something
    }
}
