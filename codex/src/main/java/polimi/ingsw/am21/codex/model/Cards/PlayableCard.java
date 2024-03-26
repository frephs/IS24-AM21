package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.Optional;
import java.util.function.Function;

public class PlayableCard extends Card {
    /**
     * The front side of the card
     */
    PlayableFrontSide frontSide;

    /**
     * The back side of the card
     */
    PlayableBackSide backSide;

    /**
     * The type of the side that has been played by the player, if any
     */
    Optional<CardSideType> playedSideType;

    /**
     * The number of corners this card is currently covering
     */
    int coveredCorners;

    /**
     * The kingdom this card is part of, if any
     */
    Optional<ResourceType> kingdom;

    /**
     * Constructor
     *
     * @param id        A unique identifier for the card
     * @param frontSide The front side
     * @param backSide  The back side
     * @param kingdom   The kingdom this card is part of, if any (use null otherwise)
     */
    public PlayableCard(int id, PlayableFrontSide frontSide, PlayableBackSide backSide, ResourceType kingdom) {
        super(id);
        this.frontSide = frontSide;
        this.backSide = backSide;
        this.kingdom = Optional.ofNullable(kingdom);
    }

    public PlayableCard(int id, PlayableFrontSide frontSide, PlayableBackSide backSide) {
        this(id, frontSide, backSide, null);
    }

    public Optional<ResourceType> getKingdom() {
        return kingdom;
    }

    public Optional<CardSideType> getPlayedSideType() {
        return playedSideType;
    }

    /**
     * Gets the currently played side, if any
     */
    public Optional<PlayableSide> getPlayedSide() {
        return playedSideType.map(type -> {
            if (type == CardSideType.FRONT) return frontSide;
            return backSide;
        });
    }

    /**
     * @param playedSideType The type of the side that has been played by the player
     */
    public void setPlayedSideType(CardSideType playedSideType) {
        this.playedSideType = Optional.of(playedSideType);
    }

    public int getCoveredCorners() {
        return coveredCorners;
    }

    public void setCoveredCorners(int coveredCorners) {
        this.coveredCorners = coveredCorners;
    }

    @Override
    public Function<PlayerBoard, Integer> getEvaluator() {
        return (playerBoard) -> this.getPlayedSide().map(side -> side.getEvaluator().apply(playerBoard, coveredCorners)).orElse(0);
    }
}
