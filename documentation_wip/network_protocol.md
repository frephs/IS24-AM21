# Network Protocol
The network protocol we designed is meant to be implemented with both RMI and Client-Server Socket functionality.
Both client and server are equipped with a message parser for serialized java objects sent through the network and a set of RMI interfaces which are meant to update the views in the client and call controller methods to update the model in the server.

## Notes on RMI 
In this documentation, only Socket Messages are rapresented as there is duality in the two approaches since every message corresponds to a remote method invocation.

## General Message Handling 

### Failed Connection Handling
After a connection is enstablished, if the servers fails to respond to a message before the timeout, the clients will try to resend the message for a maximum of 3 times. If the server still fails to respond, the client will close the connection and notify the user that the connection has been lost.

```mermaid
sequenceDiagram
    actor Client
    Client -x Server: Message (timed out)
    Client ->> Server: Message (timed out)
    Server -x Client: ConfirmMessage (lost)
    Client -x Server: Message (timed out)
    Client --> Client : Log (Connection Lost)
    Destroy Client

```

### Not allowed message handling
In the event a client sends a message for an action that the server doesn't expect or that they cannot perform in that moment and in the event a client might be modified or 'enhanced' in a way the server does not contemplate, we have messages in place to send to the  aforesaid client. 


```mermaid
sequenceDiagram
    Note over client,server: Client sends a type of message <br> that is not expected 
    client ->> server: UntimelyActionMessage
    server --) client: ActionNotAllowedMessage 
    Note over client,server: Client sends a message which <br>is not recognized by the server
    client ->> server: unknownTypeMessage
    server --)  client : unknownMessageTypeMessage
```

## Game Dynamics' Flows
### Lobby Flow
The player building process requires a series of essential steps, we report them in the following sequence diagram.

Other than `ConfirmMessage`, which is required by the client to confirm the message has been received and handled correctly, we added a series of messages whose recipients are all the clients in the lobby or in the game. They are used to update the views of the clients and to notify them of the status of the lobby, to make the lobby experience more interactive and to make player attributes validation by the user a possibility.

```mermaid
sequenceDiagram
    actor Client
    autonumber
    
    # Get the available lobbies
    Note over Client,Server : Client is in the lobby view
    Client ->> Server : GetAvailableLobbiesMessage
    Server -->> Client : AvailableLobbiesMessage
    
    # Join the Game Lobby
    Note over Client,Server : Client selects a game lobby
    loop until the selected lobby is not full
    Client -) Server : JoinLobbyMessage
    alt Lobby is full
        Server -->> Client : LobbyFullMessage
    else Lobby is not full
        Server -->> Client : ConfirmMessage
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
        Server --) Client in the lobby view:  PlayerNicknameSetMessage
    end

    # Player Token color 
    Note over Client,Server : Client selects a token color
    
    loop until the selected token color is not taken
        loop until the player has not selected a token color
            Client ->> Server : GetAvailableTokenColorsMessage
            Server -->> Client : AvailableTokenColorsMessage
        end
            Client ->> Server : SetTokenColorMessage 
            alt Token color is already taken
            Server --) Client : TokenColorAlreadyTakenMessage 
            Client ->> Server : GetAvailableTokenColorsMessage
            Server -->> Client : AvailableTokenColorsMessage
            else Token color is accepted
                Server --) Client : ConfirmMessage
        end
    end
  
    loop for each client in the lobby
        Server --) Client in the lobby view:  PlayerTokenColorSetMessage
    end
    


    # Player Secret Objective 
    Note over Client,Server: Client selects a secret objective
    Client ->> Server : GetObjectiveCardsMessage
    Server -->> Client : ObjectiveCardsMessage 
    Client -) Server : SelectFromPairMessage 
    Server ->> Client : ConfirmMessage
    

    #Player Starter Card Side to place
    Note over Client,Server: Client selects a starter card side to play

    Client ->> Server : GetStarterCardSidesMessage
    Server -->> Client : StarterCardSidesMessage
    Client --) Server : SelectFromPairMessage
    Server -->> Client : ConfirmMessage

    loop for each client in the game
    Server --) Client in the game view:  PlayerGameJoinMessage
    end
    Note over Client,Server: The player in now in the game view
    loop every 5 seconds until all players are in the game
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
    Actor Playing client
    loop until the game is over
    # New turn 
    Note over Server,Client:Current Player Changes
    loop for each client
        autonumber
        Server -) Client : PlayerStateUpdateMessage 
    end
    
    #Place card  
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
        Server --) Client : CardPlacedMessage 


    opt only if the player's score is updated
    Server --) Client : PlayerScoreUpdateMessage
    end
    Server --) Client : NextPlayerActionMessage
    end 
    

    Note over Playing client,Server: The playing client can draw a card
    ## Draw card 
    Playing client -) Server : DeckDrawMessage OR CardPairDrawMessage
    Server --) Playing client : ConfirmMessage
    loop for each client
    Server --) Client : DeckCardDrawMessage OR CardPairDrawMessage
    end 
    end



```
### Game over flow
When `Game.nextTurn()` detects that a player has a winning score or a `EmptyDeckException` is Caught by the controller, a message is sent to all the clients to notify them the number of remaining rounds.

After the final rounds are played, the server will send a series of messages to all the clients to notify them that the game is over and update the final scores of the players after adding the objective cards' points.

```mermaid
sequenceDiagram
    Note over Server, Client : Normal Turn flow interactions
    loop until game.remainingTurns is set
    Server --> Client: turn flow messages 
    end 
    Note over Server, Client: Last turn interactions
    Server -) Client : RemainingTurnsMessage 
    loop for each client 
        Server -> Client: normal turn interactions 
    end 
    
    Note over Server, Client: Game overs
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
This comunication happen when a player(Client) want to write a message in the chat. They send the postMessage to the Server that will notify the player that the message has been received and posted; later it will send a notification to all the other players(Recipient) that there is a new message in the chat.
```mermaid
sequenceDiagram
Actor Client
    Client -) Server: postMessage
    Server --) Client: confirmMessage
Actor Recipient
    loop for each recipient
        Server -) Recipient: newMessageInChat
    end
```
