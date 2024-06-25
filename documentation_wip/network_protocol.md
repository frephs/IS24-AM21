# Network Protocol
The network protocol we designed is meant to be implemented with both RMI and Client-Server Socket functionality.
Both client and server are equipped with a message parser for serialized Java objects sent through the network and a set of RMI interfaces which are meant to update the views in the client and call controller methods to update the model in the server.

## Notes on RMI 
In this documentation only Socket messages are represented, as there is duality in the two approaches since every message corresponds to a remote method invocation.

## General message handling 

### Client Action message handling
The following diagram reports a general rapresentation of our architecture's flow when a ClientAction is sent to the server.
The **listener  classes** calling `gameEventListenerMethods(...)` implement either the `RemoteGameEventListener` class or the `GameEventListener` which extends it and does not throw `RemoteException`. 

Note: for listener we mean every `RMIClientConnectionHandler`'s reference and every `TCPServerConnectionHandler` object saved for every client, which handle the connection with the client. 

```mermaid
sequenceDiagram
Actor Client 
Client  -->> ServerConnectionHandler : ClientActionMessage
ServerConnectionHandler --> Controller : Controller.gameEventListenerMethod(...)
Note right of Controller: the action is performed by the controller which <br> through the game manager  updates the correct game. 
Controller --> Controller : Action is performed 
Note over Controller, Clients: A view update is sent to every concerned client
loop For each registered listener
Controller --> Clients: listener.gameEventListenerMethod(...)
end
Clients --> ClientGameEventHandler : ClientConnectionHandler.gameEventListenerMethod(...) 
ClientGameEventHandler --> ClientGameEventHandler  : LocalModelContainer.gameEventListenerMethod(...)
ClientGameEventHandler --> ClientGameEventHandler : View.gameEventListenerMethod(...)

```

### Failed connection handling
After a connection is enstablished, the client starts sending regular heartbeats. After two missed heartbeats the client loosing connection's `ClientConnectionHandler` notifies the user while the other clients are notified by the server. The client is considered disconnected after 10 missed heartbeats.

The one reported is a simplified view with less actors, the architecture is the same as described above.



```mermaid
sequenceDiagram
    actor Client 1
    loop 2 times
    Client 1 --x Server: Heartbeat (timed out)
    end 
    Client 1 --> Client 1 : ClientConnectionHandler.failedHeartBeats++
    Client 1 --> Client 1 : View.postnotification()"You're loosing connection to the server")
    Server -->> Other clients : PlayerConnectionChangedMessage(IS_LOSING)
    Other clients --> Other clients : ClientGameEventHandler.playerConnectionChanged(...)
    alt The next heartbeats don't fail
        Client 1 --> Client 1 : ClientConnectionHandler.failedHeartBeats = 0
    Client 1 --> Client 1 : View.postnotification()"Connection restored")
    Server -->> Other clients : PlayerConnectionChangedMessage(CONNECTED)
    Other clients --> Other clients : ClientGameEventHandler.playerConnectionChanged(...)
    else 
    loop 8 times more
        Client 1 --x Server: Heartbeat 
    end 
    Client 1 --> Client 1 : ClientConnectionHandler.connectionFailed()

    Client 1 --> Client 1 : view.postNotification("Connection lost")

    Client 1 --> Client 1 : exit
    Destroy Client 1
    Client 1 --> Client 1 : 
    Server -->> Other clients: PlayerConnectionChangedMessage(DISCONNECTED)
    Other clients --> Other clients : view.ClientGameEventHandler.playerConnectionChanged(...)
    end
    

```

### "Not allowed" message handling
In the event a client sends a message for an action that the server which is untimely for the user's context  or that they cannot perform in that moment, and in the event a client might be modified or 'enhanced' in a way the server does not contemplate, we have messages in place to send to the aforesaid client. 

Client's side the error's are parsed by the `ClientGameEventHandler` which implements the `GameErrorListener` interface.

Since updates are only register by the local model once the server validates them only the `View` implements the class as the errors need only to be notified to the users. 


```mermaid
sequenceDiagram
    actor Client

    Note over Client,ServerConnectionHandler: Client sends a type of message <br> that is not expected 
    Client ->> ServerConnectionHandler: Untimely or forbidden ActionMessage 
ServerConnectionHandler ->> Controller : Controller.gameEventListenerMethod(...)
    Controller --) ServerConnectionHandler : < ? extends InvalidActionException> thrown
    ServerConnectionHandler --) Client : InvalidActionMessage
    Client --> Client : Message.toException() is parsed by the ClientGameEventHandler


    
    Note over Client,ServerConnectionHandler: Client sends a message which <br>is not recognized by the server
    Client ->> ServerConnectionHandler: <Unknown-type message>
    ServerConnectionHandler --)  Client : unknownMessageTypeMessage
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
    Client ->> Server : GetAvailableGameLobbiesMessage
    Server ->> Controller : Controller.getAvailableGameLobbies()
    alt there are no games available
        Server ->> Controller: Controller.createGame()
        Controller --) Server : Returns gameIds: List<int>
    else there are games available 
        Server ->> Controller : Controller.listGames()
        Controller --) Server : Returns gameIds: List<int>

    end 
    Server --) Client : AvailableGameLobbiesMessage
    
    # Join the Game Lobby
    Note over Client,Server : Client selects a game lobby or creates a new one
    alt Client selects a game lobby

    loop until the selected lobby is not full
        Client -) Server : JoinLobbyMessage
        alt Lobby is full
            Server ->> Controller : Controller.lobbyJoin(socketId, lobbyId)
            Controller --) Server : LobbyFullException
            Server --) Client : LobbyFullMessage
        else Lobby is not full
            Controller ->> Server : Controller.lobbyJoin(socketId, gameId)
            Controller -->Server: Returns
            Server --) Client : ConfirmMessage
        end
    end

    else Client creates a new game lobby
        Client ->> Server : CreateGameLobbyMessage
        Server ->> Controller : Controller.createGame()
        Controller --> Server : Returns 

        Server ->> Controller : Controller.lobbyJoin(socketId, gameId)

        Controller --) Server : Returns 
        Server --) Client : ConfirmMessage
    end


    # Player Nickname
    Note over Client,Server : Client selects a nickname
    loop until the selected nickname is not taken
        Client ->> Server : SetNicknameMessage
        Server ->> Controller: Controller.lobbySetNickname(socketId, nickname)
        alt Nickname is already taken   
            Controller --) Server: NicknameAlreadyTakenException
            Server --) Client : NicknameAlreadyTakenMessage
        else Nickname is accepted
            Controller --> Server : Returns 
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
            Server ->> Controller: Controller.lobbyGetAvailableTokenColors() 
            Controller --) Server : Returns tokens: List<TokenColor> 
            Server --) Client : AvailableTokenColorsMessage
        end

        Client ->> Server : SetTokenColorMessage 
        Server ->> Controller: Controller.lobbySetTokenColor(nickname, tokenColor)
        alt Token color is already taken
            Controller --) Server : NoSuchElementException
            Server --) Client : TokenColorAlreadyTakenMessage 
        else Token color is accepted
            Controller --> Server : Returns
            Server --) Client : ConfirmMessage
        end
    end   
  
    loop for each client in the lobby
        Server -) Client in the lobby view:  PlayerTokenColorSetMessage
    end
    


    # Player Secret Objective 
    Note over Client,Server: Client selects a secret objective
    Client ->> Server : GetObjectiveCardsMessage
    Server ->> Controller : Controller.lobbyGetObjectiveCards()
    Controller --> Server : Returns cardIds: Pair<int> 
    Server -->> Client : ObjectiveCardsMessage 
    
    Client -) Server : SelectFromPairMessage 
    Server ->> Controller : Controller.lobbyChooseObjectiveCard(first)
    Controller --> Server : Returns
    Server ->> Client : ConfirmMessage
    

    # Player Starter Card Side to place
    Note over Client,Server: Client selects a starter card side to play

    Client ->> Server : GetStarterCardSidesMessage
    Server ->> Controller : Controller.lobbyGetStarterCardSides()
    Controller --> Server : Returns cardId: int
    Server --) Client : StarterCardSidesMessage

    Client ->> Server : SelectFromPairMessage
    Server ->> Controller : Controller.lobbyChooseStarterCardSide(first)
    Controller --> Server : Returns
    Server --) Client : ConfirmMessage

    loop for each client in the game
    Server -) Client in the game view: PlayerGameJoinMessage
    end
    Note over Client,Server: The player in now in the game view
    Client ->> Server : GetGameStatusMessage
    
    Server ->> Controller: Controller.checkIfGameStarted()
    alt Not all players are in the game
        Controller --) Server: Returns false

        Note left of Server: No response
    else All players are in the game
        Server ->> Controller: Controller.gameStart(gameId)
        Controller --) Server: Returns true
        Server --) Client: GameStatusMessage (GAME_START)
        loop: for each client in the game view
            Server -) Client in the game view: GameStatusMessage (GAME_START)
        end
    end

```

### Normal game turns flow 
Until `Game.nextTurn()` detects that a player has a winning score, the messages between the server and the clients are exchanged as follows.

As before, other than the `ConfirmMessage`, we have a series of messages whose recipients are all the clients in the game. They are used to update the views of the clients and to notify them of the status of the player turn.

```mermaid
sequenceDiagram
    actor Playing client
    Server -) Controller: Controller.startGame()
    Server -) Client : GameStatusMessage (GAME_START) 

    loop until the game is over
        # New turn 
        Note over Server,Client: Current Player Changes
        
        # Place card  
        Note over Playing client,Server: The playing client can place a card  
        loop until the card placement is valid
            Playing client ->> Server : PlaceCardMessage
            Server --) Controller: Controller.placeCard(handIndex, side, position)
            alt card placement is not valid
                Controller --) Server: InvalidCardPlacementMessage
                Server --) Playing client : InvalidCardPlacementMessage
            else card placement is valid
                Controller --> Server: Returns
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
        Server --) Controller: Controller.drawCard()
        alt deck is empty
            Controller --) Server: EmptyDeckException
            loop for each client
                Server --) Playing client : LastRoundMessage
            end
        else deck is not empty
            Note over Playing client,Server: This notfication serves also to notify the
            Server --) Playing client : ConfirmMessage
            loop for each client
                Server -) Client : DeckCardDrawnMessage OR CardPairDrawnMessage
            end
        end
        Server -) Client : NotifyNextPlayerMessage
    end
```
### Game over flow
When `Game.nextTurn()` detects that a player has a winning score or an `EmptyDeckException` is caught by the controller, a message is sent to all the clients to notify them of the number of remaining rounds.

After the final rounds are played, the server will send a series of messages to all the clients to notify them that the game is over and update the final scores of the players after adding the objective cards' points.

```mermaid
sequenceDiagram
    participant Controller
    participant Server
    actor Client

    Note over Server: Normal Turn flow interactions
    loop until game.remainingTurns is set
        Controller --> Client : turn flow messages 
    end 

    Note over Server: Last round interactions
    Controller --> Controller: Last round reached
    Controller ->> Server: nextTurn() (lastRound)

    Server -) Client : RemainingTurnsMessage 
    loop for each client 
        Controller -> Client : normal turn interactions 
    end 
    
    Note over Server: Game overs
    Controller --> Controller: Game over
    Controller ->> Server: setGameOver
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
The chat feature works similarly to the other view updates. Every view update is sent to the concerned clients present in the `ChatMessage` object. 

```mermaid
sequenceDiagram
actor Client
Note over Client, Server: The messages can be either <br>broadcasts or whispers.
alt The message is a broadcast message
    Client --) Server: SendChatMessage
    loop for each client in the same game
        Server --) Recipient: ChatMessageMessage
    end
else The message is a private message to an user
actor Recipient
Client --) Server: SendChatMessage
 Server --) Recipient: ChatMessageSentMessage
  Server --) Client: ChatMessageMessage
    end



```
