package polimi.ingsw.am21.codex.client.localModel;

public class GameEntry {

  private final String gameId;
  private Integer currentPlayers;
  private final Integer maxPlayers;

  public GameEntry(String gameId, Integer maxPlayers) {
    this.gameId = gameId;
    this.maxPlayers = maxPlayers;
    this.currentPlayers = 0;
  }

  public GameEntry(String gameId, Integer currentPlayers, Integer maxPlayers) {
    this(gameId, maxPlayers);
    this.currentPlayers = currentPlayers;
  }

  public Integer getCurrentPlayers() {
    return currentPlayers;
  }

  public void setCurrentPlayers(Integer currentPlayers) {
    this.currentPlayers = currentPlayers;
  }

  public String getGameId() {
    return gameId;
  }

  public Integer getMaxPlayers() {
    return maxPlayers;
  }
}
