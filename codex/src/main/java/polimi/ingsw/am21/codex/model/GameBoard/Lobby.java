package polimi.ingsw.am21.codex.model.GameBoard;

import polimi.ingsw.am21.codex.model.Cards.CardPair;
import polimi.ingsw.am21.codex.model.Cards.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Player;
import polimi.ingsw.am21.codex.model.TokenColor;

import java.util.HashMap;
import java.util.UUID;

/**
 * Represents the lobby where players join before starting the game.
 */
public class Lobby {
    /**
     * The map of players in the lobby, with their socket ID as the key and PlayerBuilder as the value used to progressively create the player;
     */
    HashMap<UUID, Player.PlayerBuilder> lobbyPlayers;
    /**
     * The map of extracted cards associated with each player's socket ID.
     */
    HashMap<UUID, CardPair<ObjectiveCard>> extractedCards;

    /**
     * The remaining slots for players in the lobby. ( how many player can still join the game )
     */
    int remainingPlayerSlots = 0;

    /**
     * The array of all the token colors.
     */
    TokenColor[] tokenColors;

    /**
     * Constructs a lobby with the specified maximum number of players.
     *
     * @param maxPlayers the maximum number of players allowed in the lobby
     */
    public Lobby(int maxPlayers){
        this.remainingPlayerSlots = maxPlayers;
        this.tokenColors = TokenColor.values();
    }

    /**
     * Constructs a lobby with a default maximum number of players (4).
     */
    public Lobby(){
        this(4);
    }

    /**
     * Gets the number of remaining player slots in the lobby.
     *
     * @return the number of remaining player slots
     */
    public int getRemainingPlayerSlots() {
        return remainingPlayerSlots;
    }

    /**
     * Gets the current count of players in the lobby.
     *
     * @return the count of players in the lobby
     */
    public int getPlayersCount() {
        return lobbyPlayers.size();
    }

    /**
     * Adds a player to the lobby.
     *
     * @param socketId the socket ID of the player to add
     * @throws LobbyFullException if the lobby is full and cannot accept more players
     */
    public void addPlayer(UUID socketId) throws LobbyFullException {
        if(lobbyPlayers.size() >= remainingPlayerSlots){
            throw new LobbyFullException();
        }
        lobbyPlayers.put(socketId, new Player.PlayerBuilder());
        remainingPlayerSlots--;
    }

    /**
     * Removes a player from the lobby.
     *
     * @param socketId the socket ID of the player to remove
     * @return the extracted objective card associated with the removed player
     * @throws PlayerNotFoundException if the player is not found in the lobby
     */
    public CardPair<ObjectiveCard> removePlayer(UUID socketId) throws PlayerNotFoundException {
        if(!lobbyPlayers.containsKey(socketId)){
            throw new PlayerNotFoundException(socketId);
        }
        lobbyPlayers.remove(socketId);
        remainingPlayerSlots++;
        return extractedCards.remove(socketId);
    }

    /**
     * Sets the nickname for a player in the lobby.
     *
     * @param socketId the socket ID of the player
     * @param nickname the nickname to set
     * @throws PlayerNotFoundException if the socket ID is not found in the lobby
     * @throws NicknameAlreadyTakenException if the nickname is already taken by another player
     */
    public void setNickname(UUID socketId, String nickname) throws PlayerNotFoundException, NicknameAlreadyTakenException {
        if(!lobbyPlayers.containsKey(socketId)){
            throw new PlayerNotFoundException(socketId);
        }
        if(lobbyPlayers.keySet().stream().anyMatch(id -> !id.equals(socketId) && lobbyPlayers.get(id).getNickname().equals(nickname))){
            throw new NicknameAlreadyTakenException(nickname);
        }
        lobbyPlayers.get(socketId).setNickname(nickname);
    }

    /**
     * Sets the token color for a player in the lobby.
     *
     * @param socketId the socket ID of the player
     * @param tokenColor the token color to set
     * @throws PlayerNotFoundException if the socket ID is not found in the lobby
     * @throws TokenAlreadyTakenException if the token color is already taken by another player
     */
    public void setToken(UUID socketId, TokenColor tokenColor) throws PlayerNotFoundException, TokenAlreadyTakenException {
        if(!lobbyPlayers.containsKey(socketId)){
            throw new PlayerNotFoundException(socketId);
        }
        if(lobbyPlayers.keySet().stream().anyMatch(id -> !id.equals(socketId) && lobbyPlayers.get(id).getTokenColor().equals(tokenColor))){
            throw new TokenAlreadyTakenException(tokenColor);
        }
        lobbyPlayers.get(socketId).setTokenColor(tokenColor);
    }

    /**
     * Sets the extracted card for a player in the lobby.
     *
     * @param socketId the socket ID of the player
     * @param extractedCard the extracted card to set
     * @throws PlayerNotFoundException if the socket ID is not found in the lobby
     */
    public void setExtractedCard(UUID socketId, CardPair<ObjectiveCard> extractedCard) throws PlayerNotFoundException {
        if(!lobbyPlayers.containsKey(socketId)){
            throw new PlayerNotFoundException(socketId);
        }
        extractedCards.put(socketId, extractedCard);
    }

    /**
     * Finalizes a player in the lobby, constructing the Player object and removing the PlayerBuilder from the map
     *
     * @param socketId the socket ID of the player to finalize
     * @param objectiveCard the objective card associated with the player
     * @return the finalized Player object
     * @throws PlayerNotFoundException if the socked ID is not found in the lobby
     */
    public Player finalizePlayer(UUID socketId, ObjectiveCard objectiveCard) throws PlayerNotFoundException {
        if(!lobbyPlayers.containsKey(socketId)){
            throw new PlayerNotFoundException(socketId);
        }
        Player player = lobbyPlayers.get(socketId).build();
        lobbyPlayers.remove(socketId);
        return player;
    }
}
