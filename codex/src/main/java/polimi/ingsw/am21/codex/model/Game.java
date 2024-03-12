package polimi.ingsw.am21.codex.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Game {

    private final int MAX_PLAYERS = 4;
    private Player currentPlayer;
    private List<Player> players = new ArrayList<>(MAX_PLAYERS);
    private GameBoard;

    private GameState;

    private TokenColors tokens[] = Arrays.stream(TokenColors.values()).filter(token -> token != TokenColors.BLACK).;

    public Game(int players, String nickNames[]){

        for (int i = 0; i < players; i++) {
            players.add(
                    new Player(
                            nickNames[i],
                            )
            );
        }
    }

    public playTurn(Player currentPlayer, int deck, int source){
        PlayedCard card;
        player.playCard();

        //BOH

        source = currentPlayer.choseDrawingSource(source);
        deck = currentPlayer.choseDrawingDeck(deck);
        card = GameBoard.drawCard(source, deck);
        currentPlayer.p


    }
}
