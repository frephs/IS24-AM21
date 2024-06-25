package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import java.util.List;
import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class PlayerJoinedGameMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final UUID connectionID;
  private final String nickname;
  private final TokenColor color;
  private final List<Integer> handIDs;
  private final Integer starterCardID;
  private final CardSideType starterSideType;

  public PlayerJoinedGameMessage(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardID,
    CardSideType starterSideType
  ) {
    super(MessageType.PLAYER_JOINED_GAME);
    this.gameId = gameId;
    this.connectionID = connectionID;
    this.nickname = nickname;
    this.color = color;
    this.handIDs = handIDs;
    this.starterCardID = starterCardID;
    this.starterSideType = starterSideType;
  }

  public String getGameId() {
    return gameId;
  }

  public UUID getConnectionID() {
    return connectionID;
  }

  public String getNickname() {
    return nickname;
  }

  public TokenColor getColor() {
    return color;
  }

  public List<Integer> getHandIDs() {
    return handIDs;
  }

  public Integer getStarterCardID() {
    return starterCardID;
  }

  public CardSideType getStarterSideType() {
    return starterSideType;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId=" +
      gameId +
      ", connectionID=" +
      connectionID +
      ", nickname='" +
      nickname +
      '\'' +
      ", color=" +
      color +
      ", handIDs=" +
      handIDs +
      ", starterCardID=" +
      starterCardID +
      ", starterSideType=" +
      starterSideType +
      '}'
    );
  }
}
