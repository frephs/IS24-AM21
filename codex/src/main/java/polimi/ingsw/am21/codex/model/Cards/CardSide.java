package polimi.ingsw.am21.codex.model.Cards;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardSide {

    private final int max_corners = 4;
    List<Optional<Corner>> corners= new ArrayList<>(max_corners);



    public Corner getCorner(CornerEnum corner){
        return corners.get(CornerEnum.ordinal());
    }


}
