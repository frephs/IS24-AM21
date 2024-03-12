# UML progetto di ingegneria del software
 A rough view of the UML of Model View Controller:
- **Model**: will be connected to the server side controller
    - *Game*: enforces the games rhythm, general rules about the game status, calls for turns and rounds, creates the game boards (common and personal), instantiates the players (passing their boards as parameters) and the decks of cards, keeps track of the game state and the game over condition.
    - *Player*: can play a turn (try to play a card on the player board given as parameter to their constructor and receive drawn cards), has points, 
        - *Player Board*: enforces rules for card placement and keeps tracks of resources, objets the geometry of the cards placed onto it
    - *Cards hierarchy*: The uml is pretty self explanatory
    - *GameBoard*: composed of (composition class to keep things tidy (is it necessary tho?)
        - *Score Board*: hashmap with buckets, of (lists of) player tokens (optional utility to useful for the GUI development eventually)
        - *Common Board*: common cards for everyone to use, methods to get and draw cards from here (the latter also draws a new card from the deck) (card retrieval methods comprehend common goals retrival for point calculation). 
        - *Card's Decks* : methods to shuffle and retrieve cards from the decks.
        -
- **View**:
    - Cli: text based implementation of the game components for the client
    - Gui: javaFX implementation of the game components for the client
- **Controller**:
    - Client: will handle input received from the respective views and will submit it to the server.
    - Server: will instantiate the game and handle the communications with all the clients.

## Model

### Cards
```mermaid

classDiagram

class ResourcesTypes {
    <<Enumeration>>
    PLANT_KINGDOM
    ANIMAL_KINGDOM
    FUNGI_KINGDOM
    INSECT_KINGDOM

    +toString() String
}

class ObjectTypes{
    <<Enumeration>>
    QUILL
    INKWELL
    MANUSCRIPT

    +toString() String
}

class CornerContentTypes{
    <<Enumeration>>
    RESOURCE
    OBJECT
    +toString() String

}

class CardSides{
    <<Enumeration>>
    FRONT
    BACK
}

class Card {
    %% potrebbe essere un'interfaccia
    <<Abstract>>
    %%+createCard() void
}

class CornerEnum {
    <<Enumeration>>
    UP_LEFT
    DOWN_LEFT
    UP_RIGHT
    DOWN_RIGHT
}

class SidedCard {
    sides: Hasmap~CardSides, CardSides~ (*)
    %% cardSide[BACK] will be instanced as CardBackSide obv. 
    SidedCard(ResourceTypes []front, ResourceTypes[]back\n, ObjectTypes front[], objectTypes back[]\n, ResourceTypes permanentBackResources)
    
    %%calls the method link of the corner and adjusts the content of the corners
}


class Position{
    x: int
    y: int
    computeLinkingPosition(CornerEnum linkedCorner) Position
    Position(x,y)
    
    %% Overriding 
    equals(Position) bool
    hashCode() int
}

class PlayedCard{
    playedSide: CardSides
    card: SidedCard

    getPlayedSide() CardSides
    getAvailableCorners() CornerEnum[0..4]
}

class CardSide {
    corners: HashMap~CornerEnum, Corner~ 

    CardSide(ResourceTypes resources, ObjectTypes objects)
    
    %% You actually position cardSides not cards
    getResources() hashmap~ResourceTypes, int~
    getObjects() hashmap~Objects, int~ 
    %% devo dargli i corners come input.
}

class CardBackSide {
    permanentResources: ResourceTypes[0..3]
    CardBackSide(ResourceTypes resources \n, ResourceTypes permanentResources, ObjectTypes objects)

    %% overrides 
    getResources() hashmap~ResourceType, int~
}

class Corner~T~{
    contentType: otpional~CornerContentTypes~
    content: Optional~T~
    isCovered: bool
    %% it is important that we link a card and not a corner cause otherwise we'd have to implement something like corner.parentCard and honestly ew.

    isLinked() bool
    
    %%isLinked() bool
    %%lilinkCorner(Corner) void
    %% changes the value of linkedCorner and the value of actualContent if the content of the linked corner is different
    %%getLinkedCard() Optional~PlayedCard~
    %%getLinkedCorner() Corner~T~
    %% returns the linked corner if it's linked, otherwise an empty optional
    %%getActualContent() Optional~T~
    %% returns the content of the linked corner if it's linked, else returns the content of the corner, also depending on which card is above (the one underneath will be the one that has the linked corner)
}

%%class Linkable{
  %%  <<Interface>>
    %%-link() void
    %% couples that connect are 1-4 2-3 with transitivity
    %% come implementiamo link? 
%%}


class ResourceCard{
    %% non importante, salviamo la risorsa 
    %% kingdom: ResourceTypes
    points: Optional~int~
    evaluate()
}

class GoldCard{
    int points; 
    conditionalSet: ResourceTypes[1..5]
    pointCondition: optional~PointConditionTypes~
    %% o è meglio una lista?
    conditionalObject optional~ObjectTypes~
    %% object that if present on the player board will grant points
    evalutePoints()
    %%override
}

class PointConditionTypes{
    <<Enumeration>>
    OBJECTS
    CORNERS
}

CardSide "1"<|--"1" CardBackSide : inherits from

class StarterCard{
 
    starterCard(resourceTypes front, ResourceTypes back, ResourceType permanent)
    %% solo risorse niente oggetti negli angoli ma con
    firstPlayerToken: bool 
    %%OVERRIDE
    cardSide(Corner~ResourceTypes~ corners[]) 
    %% useful for the GUI
    
}

%%class StarterCardBackSide {
  %%  centralResources: ResourceTypes[2]
%%}

%%CardSide "1"<|--"1" %%StarterCardBackSide : extends



class ObjectiveCard{
    points: final int
    objective: Objective
    %% FIXME: va cambiato in un optional di ObbjectTYpes o di Resources
    %% Direi che va introdotta una classe che li metta assieme. 
    %% è meglio una lista?
    evaluate() int
    %% return points * objective.evaluate()
    
}

class Objective{
    <<abstract class>>
    %% how many times the objective has to be satisfied
    evaluate() int
    %% returns
    %% lo realizzeremo dentro evaluate count: int
}

class GeometricObjective{
    geometry: ResourceTypes[3][3]
    %% how many times the 
    evaluate(starterCard start) int
}

class CountingObjective{
    resources: HashMap~ResourceTypes, int~
    objects: HashMap~Objects, int~
    evaluate(HashMap~ResourceTypes,int~ resources, HashMap~ObjectsTypes,int~ objects) int
}

PlayedCard <-- CardSides : uses
PlayedCard "1" *-- "1" RelativePosition : is composed of
PlayedCard *-- SidedCard: is composed of 
GoldCard <-- PointConditionTypes : uses
CornerEnum <-- Corner : uses 
CornerEnum <-- CardSide : uses 
Card <|-- SidedCard : Inherits from
CardSide "1"*-- "4" Corner: is composed of
SidedCard "1" *-- "2" CardSide : is composed of 
SidedCard  <|-- ResourceCard : inherits from 
SidedCard <|-- StarterCard: inherits from 
Card <|-- ObjectiveCard: inherits from 

SidedCard <|-- GoldCard : inherits from
ObjectiveCard *-- Objective : is composed of
Objective <|.. GeometricObjective : realization
Objective <|.. CountingObjective : realization





```

### Game model

```mermaid

---
title: Rest of the model
---
classDiagram

class TokenColors{
    <<Enumeration>>
    RED
    BLUE
    GREEN
    YELLOW
    BLACK
    
    +toString() String
}


class Game{
    
    -tokens: Token[9] 
    -players: Player[2..4] 
    -gameBoard: GameBoard
    -?state: GameStates 

    -currentPlayer: Player

    
    Game(int players)
    %% contstructor: creates all the game assets.

    -isGameOver() bool
    %%GameOver() void
    
    
    +addPlayer(String nickname) bool
    %% TODO: decidere come gestire il caso in cui viene rifiutata la richiesta di aggiunta di un giocatore (nickname già presente o troppi giocatori)
    

    +?getGameStates() GameStates
    
    getPlayersNames()

    playTurn() void
}

%% TODO decidere se implementarlo come un obietti
class GameState_OR_PlayerStates{
    <<Enumeration>>
    GAME_INIT
    %%WAITING
    PLAYING
    GAME_OVER
}

class PlayerState{
    <<Enumeration>>
    WAITING
    PLAYING
}


class Deck~T~ {
    cards: Set~Card~ 
    %% Una pila forse
    Deck(Card[n])
    shuffle() void
    draw() Card
    draw(int) Card[*]
    cardsLeft() int
    insert(T ) void
    %% insert card back after card drawing. (ie when you don't choose an objective)
}

class Player {
    nickname: String 
    %%forse va nel client o forse si può mantenere per la persistenza del client.
    points: int
   
    token: Token
    board: playerBoard
    %%+chooseToken(Token [] availableTokens) Token maybe implemented in client?
    Player(String nickname, Token token)
    getPoints() int
    setPoints(int) void
    %% points are abviously private
    

    +drawCard(SidedCard) void
    %% receive 
    +chooseObjective(ObjectiveCard[2] availableObjectives) ObjectiveCard
    +chooseDrawingDeck()
    +chooseToken()

    playCard(int cardNumber) void
    %% removes the card from the player's hand and places it on the board calling the playerboard method
    addCard(Card card) void
    %% only puts a card in the player's hand (aka the personal board)

}

class Token {
    color: TokenColors
    Token(TokenColors)

}

class GameBoard {
    goldDeck : deck~GoldCard~
    resourceDeck: deck~ResourceCard~
    starterDeck: deck~StarterCard~
    objectiveDeck: deck~ObjectiveCard~ 
    commonBoard: CommonBoard
    scoreBoard: ScoreBoard
    GameBoard()
    %% ? +ceint[30]
}

class CommonBoard{
    goldCards: GoldCard[2]
    resourceCards: ResourceCard[2]
    objectiveCards: ObjectiveCard[2]
    
    CommonBoard()
    %% without parameters cause we'll draw 2 cards with deck.draw(2) for each type.

    drawGoldCard() GoldCard
    drawResourceCard() ResourceCard
    %% the card drawn are replaced
    getObjectiveCards() ObjectiveCard[2]
}

class ScoreBoard {
    buckets: HashMap ~int~~Token~
    addPoints(TokenColors, int) void
    getPoints(TokenColors) int
    getPoints() HashMap~TokenColors, int~

}

class PlayerBoard {
    cards: SidedCard[3]
    objectiveCards: ObjectiveCard
    playedCards: MultiKeyMap~Position~~PlayedCard~
    %% the geometry is a graph with root a link to the starter card
    resources: HashMap~ResourceType, int~
    -?AvailableCorners: List~Corner~
    %% it is useful to have a list of available corners to play a card, it is updated every time a card is played. 

    %% QUSTION maybe this goes in the controller???
    objects: HashMap~Objects, int~
    placeCard(SidedCard) bool
    evaluatePoints() int
    playCard(SidedCard card, CardSide playedSide) void
    %% updates the list of available corners (removes one and adds up to 3), places the card on the board and updates the resources and objects
    Corner(ResourceTypes)
    Corner(ObjectTypes)
}

Game "2"*--"4" Player : is composed of 
Game "1"*--"1" GameBoard : is composed of
Game "1"*--"9" Token : is composed of
Token <-- TokenColors : uses
GameBoard "1"*--"4" Deck : is composed of
GameBoard "1"*--"1" CommonBoard : is composed of
GameBoard "1"*--"1" ScoreBoard : is composed of

Player --|> Iterable : implements
Player --* PlayerBoard: composed of

```

## Considerazioni
The rationale is to implement every element that can become graphical as a separate class, so that there is a correspondence once the view is implemented. Each element will have a decorator toString to realize the cli and a method to draw it on the GUI
