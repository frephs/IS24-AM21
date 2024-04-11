package polimi.ingsw.am21.codex.model.GameBoard;

import polimi.ingsw.am21.codex.model.Cards.CardPair;
import polimi.ingsw.am21.codex.model.Cards.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Player;
import polimi.ingsw.am21.codex.model.TokenColors;

import java.util.HashMap;
import java.util.UUID;


public class Lobby {
    HashMap<UUID, Player.PlayerBuilder> lobbyPlayers;
    HashMap<UUID, CardPair<ObjectiveCard>> extractedCards;

    int remaingPlayerSlots = 0;

    TokenColors[] tokenColors;

    public Lobby(int maxPlayers){
        this.remaingPlayerSlots = maxPlayers;
        this.tokenColors = TokenColors.values();
    }

    public Lobby(){
        this(4);
    }

    public int getRemaingPlayerSlots() {
        return remaingPlayerSlots;
    }

    public int getPlayersCount() {
        return lobbyPlayers.size();
    }

    public void addPlayer(UUID socketId) throws LobbyFullException {
        if(lobbyPlayers.size()>=remaingPlayerSlots){
            throw new LobbyFullException();
        }
        lobbyPlayers.put(socketId, new Player.PlayerBuilder());
        remaingPlayerSlots--;
    }

    public CardPair<ObjectiveCard> removePlayer(UUID socketId) throws PlayerNotFoundException {
        if(!lobbyPlayers.containsKey(socketId)){
            throw new PlayerNotFoundException(socketId);
        }
        lobbyPlayers.remove(socketId);
        remaingPlayerSlots++;
        return extractedCards.remove(socketId);
    }

    public void setNickname(UUID playerID, String nickname) throws PlayerNotFoundException, NicknameAlreadyTakenException {
        if(!lobbyPlayers.containsKey(playerID)){
            throw new PlayerNotFoundException(playerID);
        }
        if(lobbyPlayers.keySet().stream().anyMatch(playerId -> !playerId.equals(playerID) && lobbyPlayers.get(playerId).getNickName().equals(nickname))){
            throw new NicknameAlreadyTakenException(nickname);
        }
        lobbyPlayers.get(playerID).setNickname(nickname);
    }
    public void setToken(UUID playerID, TokenColors tokenColor) throws PlayerNotFoundException, TokenAlreadyTakenException {
        if(!lobbyPlayers.containsKey(playerID)){
            throw new PlayerNotFoundException(playerID);
        }
        if(lobbyPlayers.keySet().stream().anyMatch(playerId -> !playerId.equals(playerID) && lobbyPlayers.get(playerId).getTokenColor().equals(tokenColor))){
            throw new TokenAlreadyTakenException(tokenColor);
        }
        lobbyPlayers.get(playerID).setTokenColor(tokenColor);
    }

    public void setExtractedCard(UUID playerID, CardPair<ObjectiveCard> extractedCard) throws PlayerNotFoundException {
        if(!lobbyPlayers.containsKey(playerID)){
            throw new PlayerNotFoundException(playerID);
        }
        extractedCards.put(playerID, extractedCard);
    }

    public Player finalizePlayer(UUID socketId,  ObjectiveCard objectiveCard) throws PlayerNotFoundException {
        if(!lobbyPlayers.containsKey(socketId)){
            throw new PlayerNotFoundException(socketId);
        }
        return lobbyPlayers.get(socketId).build();
    }

}
