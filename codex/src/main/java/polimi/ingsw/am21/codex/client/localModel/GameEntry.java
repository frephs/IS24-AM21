package polimi.ingsw.am21.codex.client.localModel;

/**
 * CLass that represents a game available in the game menu.
 * GameEntry is used to store the information about the game, such as the number of players and the maximum number of players.
 * GameEntries are stored in the local menu class
 * @see polimi.ingsw.am21.codex.client.localModel.LocalMenu
 * */
public class GameEntry {

  private final String gameId;
  private Integer currentPlayers;
  private final Integer maxPlayers;

  /**
   * GameEntry constructor. It is used to create a new empty game entry .
   * @param gameId the game id
   * @param maxPlayers the maximum number of players
   * */
  public GameEntry(String gameId, Integer maxPlayers) {
    this.gameId = gameId;
    this.maxPlayers = maxPlayers;
    this.currentPlayers = 0;
  }

  /**
   * GameEntry constructor. It is used to create a new game entry with a specified number of players already in the game
   * @param gameId the game id
   * @param currentPlayers the number of players already in the game
   * @param maxPlayers the maximum number of players
   * */
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
