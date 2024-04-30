# Message structure class diagram
## Table of Contents

- [Message structure class diagram](#message-structure-class-diagram)
  - [Client requests](#client-requests)
  - [Server responses](#server-responses)
  - [Client actions](#client-actions)
  - [View updates](#view-updates)
  - [Server errors](#server-errors)

## Class diagram

```mermaid
classDiagram
 Serializable <|-- Message : implementation

    %% SERVER MESSAFES    


    class Message {
        <<Abstract>>
        message: String    
        + String toString()*
    }


    class ConfirmMessage {
        <<Abstract>> 
    }
    Message <|-- ConfirmMessage : inheritance
    

    class ErrorMessage {
        <<Abstract>> 
    }

    Message <|-- ErrorMessage : inheritance
    

    class ViewUpdatingMessage{
        <<Abstract>>
        +playerId: int
    }

    Message <|-- ViewUpdatingMessage : inheritance

    

    class ActionMessage {
        <<Abstract>> 
    }

    class RequestMessage {
        <<Abstract>> 
    }

    class ResponseMessage {
        <<Abstract>> 
    }
    

    

    %% CLIENT MESSAGES

    Message <|-- ActionMessage : inheritance
    Message <|-- RequestMessage : inheritance
    Message <|-- ResponseMessage : inheritance
    

```



## Client requests 
```mermaid
classDiagram
    class RequestMessage {
        <<Abstract>> 
    }

    namespace Game{
        class GetGameStatusMessage{

        }
    }
    
    namespace Lobby {

        class GetAvailableGameLobbiesMessage {
            
        }
        class GetAvailableTokenColorsMessage {
        
        }

        class GetObjectiveCardsMessage {

        }

        class GetStarterCardSidesMessage{

        }

    } 

    RequestMessage <|-- GetAvailableGameLobbiesMessage : realization
        RequestMessage <|-- GetAvailableTokenColorsMessage : realization
        RequestMessage <|-- GetObjectiveCardsMessage : realization
        RequestMessage <|-- GetStarterCardSidesMessage : realization

    
    %% game
    RequestMessage <|-- GetGameStatusMessage : realization



```

## Server responses 
```mermaid
classDiagram 
    class ResponseMessage {
        <<Abstract>> 
    }
    namespace Lobby {
        class AvailableGameLobbiesMessage{
            list: list~GameID~
        }

        class AvailableTokenColorsMessage{
            tokenColors: List~TokenColor~
        }

        class ObjectiveCardsMessage{
            first_id: int
            second_id: int
        }

        class StarterCardSidesMessage{
            card_id: int
        }
        
    
    }

    namespace Game{
        class GameStatusMessage{
            Gamestate
        }
    }

    ResponseMessage <|-- AvailableGameLobbiesMessage : realization
    ResponseMessage <|-- AvailableTokenColorsMessage : realization
    ResponseMessage <|-- ObjectiveCardsMessage : realization
    ResponseMessage <|-- StarterCardSidesMessage : realization
    ResponseMessage <|-- GameStatusMessage : realization
    
```


## Client actions 
```mermaid
classDiagram
    class ActionMessage {
        <<Abstract>> 
    }

    namespace Lobby{
        class JoinLobbyMessage {
        + lobbyId: int
        }

        class SetNicknameMessage{
            nickname: String
        }

        class SetTokenColorMessage {
            color: TokenColor
        }

        
        class SelectFromPairMessage {
            first: Boolean
        }
    
    }

    ActionMessage <|-- JoinLobbyMessage : inheritance
    ActionMessage <|-- SetNicknameMessage : inheritance
    ActionMessage <|-- SetTokenColorMessage : inheritance
    ActionMessage <|-- SelectFromPairMessage: inheritance

    namespace Game{
    
    class PlaceCardMessage{
        +x: int
        +y: int
        +handIndex: int 
        +side: cardSideType 
    }

    class DeckDrawCardMessage{
        +deck: DrawingDeckType
    }

    class CardPairDrawMessage {
        +first: Boolean

    }
    }
    
    ActionMessage <|-- PlaceCardMessage: inheritance
    ActionMessage <|-- DeckDrawCardMessage: inheritance

    DeckDrawCardMessage <|-- CardPairDrawMessage  : inheritance

```

## View updates 
```mermaid
classDiagram
    class ViewUpdatingMessage{
        <<Abstract>>
        +playerId: int
    }

     namespace Lobby {
        class PlayerNicknameSetMessage {
            + nickname: String
        }

        class TokenColorSetMessage {
            + color: TokenColorType
        }

        class PlayerGameJoinMessage{
            + lobbyId: int
        }
    }
    ViewUpdatingMessage <|-- PlayerNicknameSetMessage  : realization
    ViewUpdatingMessage <|-- TokenColorSetMessage  : realization
    ViewUpdatingMessage <|-- PlayerGameJoinMessage  : realization


    namespace Game{
        class CardPlacedMessage{
            + x: int
            + y: int
            + side: cardSideType
            + cardId: int
        }

        class DeckCardDrawnMessage{
            + deck: DrawingDeckType
        }

        class CardPairDrawnMessage{
            + first: Boolean
            + deck: DrawingDeckType
            + newCardId: int
        }

        class RemainingTurnsMessage {
            + turns: int
        }

        class GameOverMessage {
        
        }

        class PlayerScoreUpdateMessage {
            + delta: int
        }

        class WinningPlayerMessage {
            winnerNickname: String
        }

        class DeckCardDrawnMessage{
            + cardId: int
        }
        class CardPairDrawnMessage{
            + cardId: int
        }

    }
    
    ViewUpdatingMessage <|-- CardPlacedMessage  : realization
    ViewUpdatingMessage <|-- DeckCardDrawnMessage  : realization
    ViewUpdatingMessage <|-- CardPairDrawnMessage  : realization
    ViewUpdatingMessage <|-- RemainingTurnsMessage  : realization
    ViewUpdatingMessage <|-- GameOverMessage  : realization
    ViewUpdatingMessage <|-- PlayerScoreUpdateMessage  : realization
    ViewUpdatingMessage <|-- WinningPlayerMessage  : realization
    ViewUpdatingMessage <|-- DeckCardDrawnMessage  : realization
    ViewUpdatingMessage <|-- CardPairDrawnMessage  : realization
    
```

## Server errors
```mermaid
classDiagram

    class ErrorMessage {
        <<Abstract>>    
    }
    
    class ActionNotAllowedMessage {

    }

    class UnknownMessageTypeMessage {
        
    }

    ErrorMessage <|-- ActionNotAllowedMessage : realization
    ErrorMessage <|-- UnknownMessageTypeMessage : realization

    namespace Lobby {
        class GameFullMessages {

        }

        class NicknameAlreadyTakenMessage {
            
        }

        class TokenColorAlreadyTakenMessage {
            
        }
    }

    ErrorMessage <|-- GameFullMessages : realization
    ErrorMessage <|-- NicknameAlreadyTakenMessage : realization
    ErrorMessage <|-- TokenColorAlreadyTakenMessage : realization


    namespace Game {
        class InvalidCardPlacementMessage {
            cardId: int
        }
    }

    ErrorMessage <|-- InvalidCardPlacementMessage : realization



```