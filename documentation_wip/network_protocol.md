# Network Protocol
The network protocol we designed is meant to be implemented with both RMI and Client-Server Socket functionality.
Both client and server are equipped with a message parser and a set of RMI interfaces which are meant to update the views in the client and call controller methods to update the model in the server.

## Notes on RMI 
In this documentation, only Socket Messages are rapresented as there is duality in the two approaches since every message corresponds to a remote method invocation.

### Player Lobby Flow
As the Player building process is divided in essential and sequantial steps, every message different from the last one received is meant to be accepted as a confirm the last phase was successful and that the client can move forward.

```mermaid
sequenceDiagram
    actor Client
    autonumber
    # Get the available lobbies
    Note over Client,Server : Client is in the lobby view
    Client ->> Server : GetAvailableLobbiesMessage
    Server -->> Client : AvailableLobbiesMessage
    
    # Join the Game Lobby
    Note over Client,Server : Client selects a lobby
    Client -) Server : JoinLobbyMessage
    Server -->> Client : LobbyJoinedMessage

    # Player Nickname
    Note over Client,Server : Client selects a nickname
    loop until the value is acceptable
    Client ->> Server : SetNicknameMessage
    alt Nickname is already taken   
        Server --) Client : NicknameAlreadyTakenMessage
    else Nickname is accepted
        loop for each client in the lobby
        Server --) Client in the lobby:  PlayerNicknameSetMessage
    end
    end

    
    end

    # Player Token color 
    Note over Client,Server : Client selects a token color
    
    loop until the value is acceptable
        loop every 2 seconds
            Client ->> Server : GetAvailableTokenColorsMessage
            Server -->> Client : AvailableTokenColorsMessage
        end
            Client ->> Server : SetTokenColorMessage 
            alt Token color is already taken
            Server --) Client : TokenColorAlreadyTakenMessage 
            Client ->> Server : GetAvailableTokenColorsMessage
            Server -->> Client : AvailableTokenColorsMessage
            else Token color is accepted
                loop for each client in the lobby
                    Server --) Client in the lobby:  PlayerTokenColorSetMessage
                end
        end
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
    Destroy Client
    Server ->> Client : ConfirmMessage
    Note over Client,Server: Client now joins the game.
    loop for each client in the game
    Server --) Client already in game:  PlayerGameJoinMessage
    end

```

## Normal game turns flow 
During the game, every Game and Player status change is notified to all clients so that a popup or a text message can appear in the view and the client can better follow the flow of the game. 
After every player move, the server sends a message to all the clients.
This message confirms to the client that the previous message has been handled correctly and notifies all the other clients of the update

```mermaid
sequenceDiagram
    # Game Start
    autonumber
    Server ->> Client : GameStatusChangeMessage (START)
    
    Server -) Client : PlayerStatusChange (populated with nickname)
    
    loop until a confirm is received 
        Playing client --) Server : PlaceCardMessage
    end
    loop for each client
        Server --) Client : CardPlacedMessage (CONFIRMS)
    Note over Client: updating views,  conferming the card placement.
    opt only if the player's score is updated
    Server --) Client : PlayerScoreUpdateMessage
    end
    Server --) Client : NextPlayerActionMessage
    end 
    

    Playing client -) Server : DeckDrawMessage OR CardPairDrawMessage
    loop for each client
    Server --) Client : DeckCardDrawMessage OR CardPairDrawMessage
    end 
    Note over Client: updating views,  conferming the card draw.




```
## Game over flow
The game flows until an GameOverException is caught by the controller. In that case the game enters a GameOver state;
```mermaid
sequenceDiagram
    Note over Server, client : Normal Turn flow interactions
    loop until a GameOverException is thrown
    Server --> client: turn flow messages 
    end 
    Note over Server, client: A Game Over Exception <br> is thrown in the model
    Server -) client : RemainingTurnsMessage 
    loop for each client 
        Server -> client: normal turn interactions 
    end 
    
    loop for each client 
        Server -) client : GameOverMessage
        loop for each player
            loop for each objective card 
                Server -) client : PlayerScoreUpdateMessage
            end 
        end
        Server -) client : WinningPlayerMessage  
    end

```

## Not-allowed messages handling
In the event a player tries to place cards or draw in a turn that isn't his or in the event a client might be modified or 'enhanced' in a way the server nor the game contemplate, we have messages in place to send to the  aforesaid client. 


```mermaid
sequenceDiagram
    Note over client,server: Client is not the current player 
    client -) server: placeCardMessage
    server --) client: requestNotAllowedMessage 
    Note over client,server: Client sends a message which <br>is not recognized by the server
    client -) server: unknownTypeMessage
    server --)  client : unknownMessageTypeMessage

```

## Chat
This comunication happen when a player(Client) want to write a message in the chat. They send the postMessage to the Server that will notify the player that the message has been received and posted; later it will send a notification to all the other players(Recipient) that there is a new message in the chat.
```mermaid
sequenceDiagram
    Client -) Server: postMessage
    Server -) Client: messagePosted

    loop for each recipient
        Server -) Recipient: newMessageInChat
    end
```
