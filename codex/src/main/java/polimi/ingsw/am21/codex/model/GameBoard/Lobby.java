package polimi.ingsw.am21.codex.model.GameBoard;

import polimi.ingsw.am21.codex.model.Cards.CardPair;
import polimi.ingsw.am21.codex.model.Cards.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Player;
import polimi.ingsw.am21.codex.model.TokenColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the lobby where players join before starting the game.
 */
public class Lobby {
    /**
     * The map of players in the lobby, with their socket ID as the key and PlayerBuilder as the value used to progressively create the player;
     */
    private Map<UUID, Player.PlayerBuilder> lobbyPlayers;
    /**
     * The map of extracted cards associated with each player's socket ID.
     */
    private Map<UUID, CardPair<ObjectiveCard>> extractedCards;

    /**
     * The remaining slots for players in the lobby. ( how many player can still join the game )
     */
    private int remainingPlayerSlots;

    /**
     * The array of all the token colors.
     */
    private TokenColor[] tokenColors;

    /**
     * Constructs a lobby with the specified maximum number of players.
     *
     * @param maxPlayers the maximum number of players allowed in the lobby
     */
    public Lobby(int maxPlayers) {
        this.remainingPlayerSlots = maxPlayers;
        this.tokenColors = TokenColor.values();
    }

    /**
     * Constructs a lobby with a default maximum number of players (4).
     */
    public Lobby() {
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
        if (lobbyPlayers.size() >= remainingPlayerSlots) {
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
        if (!lobbyPlayers.containsKey(socketId)) {
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
     * @throws PlayerNotFoundException       if the socket ID is not found in the lobby
     * @throws NicknameAlreadyTakenException if the nickname is already taken by another player
     */
    public void setNickname(UUID socketId, String nickname) throws PlayerNotFoundException, NicknameAlreadyTakenException {
        if (!lobbyPlayers.containsKey(socketId)) {
            throw new PlayerNotFoundException(socketId);
        }
        if (lobbyPlayers.keySet().stream().anyMatch(id -> !id.equals(socketId) && lobbyPlayers.get(id).getNickname().equals(nickname))) {
            throw new NicknameAlreadyTakenException(nickname);
        }
        lobbyPlayers.get(socketId).setNickname(nickname);
    }

    /**
     * Sets the token color for a player in the lobby.
     *
     * @param socketId   the socket ID of the player
     * @param tokenColor the token color to set
     * @throws PlayerNotFoundException    if the socket ID is not found in the lobby
     * @throws TokenAlreadyTakenException if the token color is already taken by another player
     */
    public void setToken(UUID socketId, TokenColor tokenColor) throws PlayerNotFoundException, TokenAlreadyTakenException {
        if (!lobbyPlayers.containsKey(socketId)) {
            throw new PlayerNotFoundException(socketId);
        }
        if (lobbyPlayers.keySet().stream().anyMatch(id -> !id.equals(socketId) && lobbyPlayers.get(id).getTokenColor().equals(tokenColor))) {
            throw new TokenAlreadyTakenException(tokenColor);
        }
        lobbyPlayers.get(socketId).setTokenColor(tokenColor);
    }

    /**
     * Sets the extracted card for a player in the lobby.
     *
     * @param socketId      the socket ID of the player
     * @param playerExtractedCards the extracted card to set
     * @throws PlayerNotFoundException if the socket ID is not found in the lobby
     */
    public void setExtractedCard(UUID socketId, CardPair<ObjectiveCard> playerExtractedCards) throws PlayerNotFoundException {
        if (!lobbyPlayers.containsKey(socketId)) {
            throw new PlayerNotFoundException(socketId);
        }
        extractedCards.put(socketId, playerExtractedCards);
    }

    /**
     * Finalizes a player in the lobby, constructing the Player object and removing the PlayerBuilder from the map
     *
     * @param socketId      the socket ID of the player to finalize
     * @param objectiveCard the objective card associated with the player
     * @return the finalized Player object
     * @throws PlayerNotFoundException if the socked ID is not found in the lobby
     */
    public Player finalizePlayer(UUID socketId, ObjectiveCard objectiveCard) throws PlayerNotFoundException, IncompletePlayerBuilderException {
        if (!lobbyPlayers.containsKey(socketId)) {
            throw new PlayerNotFoundException(socketId);
        }
        Player.PlayerBuilder playerBuilder = lobbyPlayers.get(socketId);

        IncompletePlayerBuilderException.checkPlayerBuilder(playerBuilder);

        playerBuilder.setObjectiveCard(objectiveCard);
        Player player = playerBuilder.build();
        lobbyPlayers.remove(socketId);
        return player;
    }

    /**
     * Retrieves the objective cards associated with a player in the lobby.
     *
     * @param socketId the socket ID of the player
     * @return the CardPair containing the objective cards of the player
     */
    public CardPair<ObjectiveCard> getPlayerObjectiveCards(UUID socketId) {
        return this.extractedCards.get(socketId);
    }

    /**
     * Checks if a player with the given socket ID is already in the lobby.
     *
     * @param socketId the socket ID of the player to check
     * @return true if the player is already in the lobby, otherwise false
     */
    public boolean containsSocketID(UUID socketId) {
        return lobbyPlayers.containsKey(socketId);
    }

    /**
     * Gets the nickname of the player in the lobby.
     *
     * @param socketId the socket ID of the player
     * @return the nickname of the player
     * @throws PlayerNotFoundException if the player is not found in the lobby
     */
    public String getPlayerNickname(UUID socketId) throws PlayerNotFoundException {
        if (!lobbyPlayers.containsKey(socketId)) {
            throw new PlayerNotFoundException(socketId);
        }
        return lobbyPlayers.get(socketId).getNickname();
    }

    /**
     * Gets the token color of the player in the lobby.
     *
     * @param socketId the socket ID of the player
     * @return the token color of the player
     * @throws PlayerNotFoundException if the player is not found in the lobby
     */
    public TokenColor getPlayerTokenColor(UUID socketId) throws PlayerNotFoundException {
        if (!lobbyPlayers.containsKey(socketId)) {
            throw new PlayerNotFoundException(socketId);
        }
        return lobbyPlayers.get(socketId).getTokenColor();
    }
}