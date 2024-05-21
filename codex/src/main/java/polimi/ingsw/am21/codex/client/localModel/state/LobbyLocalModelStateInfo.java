package polimi.ingsw.am21.codex.client.localModel.state;

public class LobbyLocalModelStateInfo implements LocalModelStateInfo {

  private final String gameId;

  public LobbyLocalModelStateInfo(String gameId) {
    this.gameId = gameId;
  }

  public String getGameId() {
    return this.gameId;
  }

  @Override
  public LocalModelState getType() {
    return LocalModelState.LOBBY;
  }
}
