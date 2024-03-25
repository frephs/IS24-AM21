package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.ArrayList;
import java.util.List;

public class PlayableBackSide extends PlayableSide {
    final List<ResourceType> permanentResources;

    public PlayableBackSide(List<ResourceType> permanentResources) {
        this.permanentResources = new ArrayList<>(permanentResources);
    }

    public List<ResourceType> getPermanentResources() {
        return permanentResources;
    }

    public int evaluate(PlayerBoard playerBoard) {
        return 0;
    }
}
