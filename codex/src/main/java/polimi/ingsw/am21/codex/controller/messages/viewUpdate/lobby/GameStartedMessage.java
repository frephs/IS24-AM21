package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;

public class GameStartedMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final List<String> playerIds;
  Pair<Integer, Integer> goldCardPairIds;
  Pair<Integer, Integer> resourceCardPairIds;
  Pair<Integer, Integer> commonObjectivesIds;

  public GameStartedMessage(
    String gameId,
    List<String> playerIds,
    Pair<Integer, Integer> goldCardPairIds,
    Pair<Integer, Integer> resourceCardPairIds,
    Pair<Integer, Integer> commonObjectivesIds
  ) {
    super(MessageType.GAME_STARTED);
    this.gameId = gameId;
    this.playerIds = playerIds != null ? new ArrayList<>(playerIds) : List.of();
    this.goldCardPairIds = goldCardPairIds;
    this.resourceCardPairIds = resourceCardPairIds;
    this.commonObjectivesIds = commonObjectivesIds;
  }

  public String getGameId() {
    return gameId;
  }

  public List<String> getPlayerIds() {
    return playerIds;
  }

  public Pair<Integer, Integer> getGoldCardPairIds() {
    return goldCardPairIds;
  }

  public Pair<Integer, Integer> getResourceCardPairIds() {
    return resourceCardPairIds;
  }

  public Pair<Integer, Integer> getCommonObjectivesIds() {
    return commonObjectivesIds;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId='" +
      gameId +
      "', players=" +
      playerIds +
      ", goldCardPairIds=" +
      goldCardPairIds +
      ", resourceCardPairIds=" +
      resourceCardPairIds +
      ", commonObjectivesIds=" +
      commonObjectivesIds +
      '}'
    );
  }
}
