package polimi.ingsw.am21.codex.model.exceptions;

import java.util.Optional;
import java.util.UUID;

public class PlayerNotFoundGameException extends Exception {

  private final UUID playerID;
  private final String nickname;
  private final String identifier;
  private final Boolean isNickname;

  public PlayerNotFoundGameException(UUID playerID) {
    this(playerID, null, playerID.toString(), false);
  }

  public PlayerNotFoundGameException(String nickname) {
    this(null, nickname, nickname, true);
  }

  private PlayerNotFoundGameException(
    UUID playerID,
    String nickname,
    String identifier,
    Boolean isNickname
  ) {
    //    super("Player with nickname " + nickname + " not found");
    super("Player " + identifier + " not found");
    this.playerID = playerID;
    this.nickname = nickname;
    this.identifier = identifier;
    this.isNickname = isNickname;
  }

  public Optional<UUID> getPlayerID() {
    return Optional.ofNullable(playerID);
  }

  public Optional<String> getNickname() {
    return Optional.ofNullable(nickname);
  }

  public String getIdentifier() {
    return identifier;
  }

  public Boolean isNickname() {
    return isNickname;
  }
}
