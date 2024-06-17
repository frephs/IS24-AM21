package polimi.ingsw.am21.codex.connection.client.RMI.common;

import java.util.Optional;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public final class GamePlayerInfo {

  public final String nickname;
  public final TokenColor token;
  public final Boolean selectedObjective;
  public final Boolean joinedGame;

  public GamePlayerInfo(
    String nickname,
    TokenColor token,
    Boolean selectedObjective,
    Boolean joinedGame
  ) {
    this.nickname = nickname;
    this.token = token;
    this.selectedObjective = selectedObjective;
    this.joinedGame = joinedGame;
  }

  public Optional<String> getNickname() {
    return Optional.ofNullable(nickname);
  }

  public Optional<TokenColor> getToken() {
    return Optional.ofNullable(token);
  }

  public Boolean hasSelectedObjective() {
    return selectedObjective;
  }

  public Boolean hasJoinedGame() {
    return joinedGame;
  }
}
