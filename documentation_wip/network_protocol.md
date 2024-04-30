# Network Protocol
The network protocol we designed is meant to be implemented with both RMI and Client-Server Socket functionality.
Both client and server are equipped with a message parser for serialized Java objects sent through the network and a set of RMI interfaces which are meant to update the views in the client and call controller methods to update the model in the server.

## Notes on RMI 
In this documentation only Socket messages are represented, as there is duality in the two approaches since every message corresponds to a remote method invocation.

## General message handling 

### Failed connection handling
After a connection is enstablished, if the servers fails to respond to a message before the timeout, the clients will try to resend the message for a maximum of 3 times. If the server still fails to respond, the client will close the connection and notify the user that the connection has been lost.

```mermaid
sequenceDiagram
    actor Client
    Client -x Server: Message (timed out)
    Client ->> Server: Message (no response)
    Server --x Client: ConfirmMessage (lost)
    Client -x Server: Message (timed out)
    Client --> Client : Log (Connection Lost)
    Destroy Client

```

### "Not allowed" message handling
In the event a client sends a message for an action that the server doesn't expect or that they cannot perform in that moment, and in the event a client might be modified or 'enhanced' in a way the server does not contemplate, we have messages in place to send to the aforesaid client. 


```mermaid
sequenceDiagram
    actor Client

    Note over Client,Server: Client sends a type of message <br> that is not expected 
    Client ->> Server: <Unexpected message>
    Server --) Client: ActionNotAllowedMessage 

    Note over Client,Server: Client sends a message which <br>is not recognized by the server
    Client ->> Server: <Unknown-type message>
    Server --)  Client : unknownMessageTypeMessage
```

## Game Dynamics' Flows
### Lobby Flow
The player building process requires a series of essential steps, which are reported in the following sequence diagram.

Other than `ConfirmMessage`, which is required by the client to confirm the message has been received and handled correctly, we added a series of messages whose recipients are all the clients in the lobby or in the game. They are used to update the views of the clients and to notify them of the status of the lobby.

```mermaid
sequenceDiagram
    actor Client
    autonumber
    
    # Get the available lobbies
    Note over Client,Server : Client is in the lobby view
    Client ->> Server : GetAvailableLobbiesMessage
    Server --) Client : AvailableLobbiesMessage
    
    # Join the Game Lobby
    Note over Client,Server : Client selects a game lobby
    loop until the selected lobby is not full
        Client -) Server : JoinLobbyMessage
        alt Lobby is full
            Server --) Client : LobbyFullMessage
        else Lobby is not full
            Server --) Client : ConfirmMessage
        end
    end

    # Player Nickname
    Note over Client,Server : Client selects a nickname
    loop until the selected nickname is not taken
        Client ->> Server : SetNicknameMessage
        alt Nickname is already taken   
            Server --) Client : NicknameAlreadyTakenMessage
        else Nickname is accepted
            Server --) Client : ConfirmMessage
        end    
    end
    loop for each client in the lobby
        Server -) Client in the lobby view:  PlayerNicknameSetMessage
    end

    # Player Token color 
    Note over Client,Server : Client selects a token color
    
    loop until the selected token color is not taken
        loop until the player has not selected a token color
            Client ->> Server : GetAvailableTokenColorsMessage
            Server --) Client : AvailableTokenColorsMessage
        end

        Client ->> Server : SetTokenColorMessage 
        alt Token color is already taken
            Server --) Client : TokenColorAlreadyTakenMessage 
        else Token color is accepted
            Server --) Client : ConfirmMessage
        end
    end   
  
    loop for each client in the lobby
        Server -) Client in the lobby view:  PlayerTokenColorSetMessage
    end
    


    # Player Secret Objective 
    Note over Client,Server: Client selects a secret objective
    Client ->> Server : GetObjectiveCardsMessage
    Server -->> Client : ObjectiveCardsMessage 
    Client -) Server : SelectFromPairMessage 
    Server ->> Client : ConfirmMessage
    

    # Player Starter Card Side to place
    Note over Client,Server: Client selects a starter card side to play

    Client ->> Server : GetStarterCardSidesMessage
    Server --) Client : StarterCardSidesMessage
    Client ->> Server : SelectFromPairMessage
    Server --) Client : ConfirmMessage

    loop for each client in the game
    Server -) Client in the game view: PlayerGameJoinMessage
    end
    Note over Client,Server: The player in now in the game view
    loop every 2 seconds until all players are in the game
        Client ->> Server : GetGameStatusMessage
        alt Not all players are in the game
            Server --) Client: GameStatusMessage (GAME_INIT)
        else All players are in the game
            Server -) Client: GameStatusMessage (GAME_START)
        end
    end

```

### Normal game turns flow 
Until `Game.nextTurn()` detects that a player has a winning score, the messages between the server and the clients are exchanged as follows.

As before, other than the `ConfirmMessage`, we have a series of messages whose recipients are all the clients in the game. They are used to update the views of the clients and to notify them of the status of the player turn.

```mermaid
sequenceDiagram
    actor Playing client

    loop until the game is over
        # New turn 
        Note over Server,Client: Current Player Changes
        loop for each client
            autonumber
            Server -) Client : PlayerStateUpdateMessage 
        end
        
        # Place card  
        Note over Playing client,Server: The playing client can place a card  
        loop until the card placement is valid
            Playing client ->> Server : PlaceCardMessage
            alt card placement is not valid
                Server --) Playing client : InvalidCardPlacementMessage
            else card placement is valid
                Server --) Playing client : ConfirmMessage
            end
        end
        loop for each client
            Server -) Client : CardPlacedMessage 
            opt only if the player's score is updated
                Server -) Client : PlayerScoreUpdateMessage
            end
            Server -) Client : NextPlayerActionMessage
        end 
        

        # Draw card 
        Note over Playing client,Server: The playing client can draw a card
        Playing client ->> Server : DeckDrawMessage OR CardPairDrawMessage
        Server --) Playing client : ConfirmMessage
        loop for each client
            Server -) Client : DeckCardDrawnMessage OR CardPairDrawnMessage
        end 
    end



```
### Game over flow
When `Game.nextTurn()` detects that a player has a winning score or an `EmptyDeckException` is caught by the controller, a message is sent to all the clients to notify them of the number of remaining rounds.

After the final rounds are played, the server will send a series of messages to all the clients to notify them that the game is over and update the final scores of the players after adding the objective cards' points.

```mermaid
sequenceDiagram
    Note over Server,Client: Normal Turn flow interactions
    loop until game.remainingTurns is set
        Server --> Client : turn flow messages 
    end 

    Note over Server,Client: Last turn interactions
    Server -) Client : RemainingTurnsMessage 
    loop for each client 
        Server -> Client : normal turn interactions 
    end 
    
    Note over Server,Client: Game overs
    loop for each client 
        Server -) Client : GameOverMessage
        loop for each player
            opt if the player's score is updated
                Server -) Client : PlayerScoreUpdateMessage
            end
        end
        Server -) Client : WinningPlayerMessage  
    end
```


## Advanced Features
### Chat
This exchange happens when a player (`Client`) wants to write a message in the chat. After `PostMessage` is sent, the server replies that the message has been received and posted. After that, the server sends a notification to all the recipients of the message informing them that there is a new message in the chat.

```mermaid
sequenceDiagram
actor Client
    Client -) Server: PostMessage
    Server --) Client: ConfirmMessage
actor Recipient
    loop for each recipient
        Server -) Recipient: NewMessageInChatMessage
    end
```
