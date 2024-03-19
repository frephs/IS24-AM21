# UML progetto di ingegneria del software

A rough view of the UML of Model View Controller:

- **Model**: will be connected to the server side controller
  - _Game_: enforces the games rhythm, general rules about the game status, calls for turns and rounds, creates the game boards (common and personal), instantiates the players (passing their boards as parameters) and the decks of cards, keeps track of the game state and the game over condition.
  - _Player_: can play a turn (try to play a card on the player board given as parameter to their constructor and receive drawn cards), has points,
    - _Player Board_: enforces rules for card placement and keeps tracks of resources, objets the geometry of the cards placed onto it
  - _Cards hierarchy_: The uml is pretty self explanatory
  - _GameBoard_: composed of (composition class to keep things tidy (is it necessary tho?)
    - _Score Board_: hashmap with buckets, of (lists of) player tokens (optional utility to useful for the GUI development eventually)
    - _Common Board_: common cards for everyone to use, methods to get and draw cards from here (the latter also draws a new card from the deck) (card retrieval methods comprehend common goals retrival for point calculation).
    - _Card's Decks_ : methods to shuffle and retrieve cards from the decks.
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

class CardSidesTypes{
    <<Enumeration>>
    FRONT
    BACK
}

class CornerEnum {
    <<Enumeration>>
    UP_LEFT
    DOWN_LEFT
    UP_RIGHT
    DOWN_RIGHT
}

class Card {
    %% potrebbe essere un'interfaccia
    <<Abstract>>
    %%+createCard() void
}

class CardPair~T~ {
    -first: T
    -second: T
    CardPair(T first, T second)
    +getFirst() T
    +getSecond() T
    +replaceFirst(T) T
    +replaceSecond(T) T
    +swap() void
}

class PlayedCard{
    playedSide: CardSidesTypes
    card: SidedCard

    PlayedCard(CardSidesTypes side, SidedCard playedCard)
    getPlayedSide() CardSide
    getAvailableCorners() CornerEnum[0..4]

}
class SidedCard {
    sides: Hashmap~CardSidesTypes, CardSide~ (*)
    %% cardSide[BACK] will be instanced as CardBackSide obv. as reported below
    SidedCard(CardSide front, CardSideBack back)
}

class CardSide {
    corners: HashMap~CornerEnum, Corner~

    CardSide()

    setCorner(CornerEnum position, Corner corner);
    %% adds a corner to the hashmap (so to the card side)

    getResources() hashmap~ResourceTypes, int~
    getObjects() hashmap~Objects, int~
}

class CardBackSide {
    permanentResources: ResourceTypes[1..3]

    CardBackSide(ResourceTypes permanentResources[1..3])
    %%calls super and then instancies permanent resources

    getResources() hashmap~ResourceType, int~
    %% overrides super returning also permanent resources
}

class Corner~T~{
    contentType: Optional~CornerContentTypes~
    %%set in the constructor
    content: Optional~T~

    Corner(T content)

    isCovered: bool
    isEmpty() bool
}


class ResourceCard{
    points: Optional~int~

    ResourceCard(SidedCard card, int points)
    ResourceCard(SidedCard card)

    evaluate()
}

class GoldCard{
    int points;
    conditionalSet: ResourceTypes[1..5]
    pointCondition: optional~PointConditionTypes~
    conditionalObject optional~ObjectTypes~

    GoldCard(SidedCard card, int points, ResourceTypes[1..5] conditionalSet, \nPointConditionTypes pointCondition, ObjectTypes conditionalObject)

    GoldCard(SidedCard card, int points, ResourceTypes[1..5] conditionalSet, \nPointConditionTypes pointCondition)

    GoldCard(SidedCard card, int points, ResourceTypes[1..5] conditionalSet)

    %%FIXME: c'è un modo migliore per non usare l'enum PointConditionTypes qui?

    evaluate()
    isPlaceable(Hashmap~ResourceTypes,int~ resources) bool


    %% similar to the resource card but does not extend it because points here are mandatory
}

class PointConditionTypes{
    <<Enumeration>>
    OBJECTS
    CORNERS
}

class StarterCard{
    %%FIXME: trovare il modo di indicare che può avere solo risorse negli angoli
    %%perchè così è inutile come classe.

    starterCard(SidedCard card)
    %% solo risorse niente oggetti negli angoli ma con
    firstPlayerToken: bool
    setFirstPlayerToken(bool) void
    %%OVERRIDE
    cardSide(Corner~ResourceTypes~ corners[])
    %% useful for the GUI

}


class ObjectiveCard{
    points: final int
    objective: Objective

    ObjectiveCard(Objective objective)
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
    evaluate() int
    GeometricObjective(ResourceTypes[3][3] geometry)
}

class CountingObjective{
    resources: HashMap~ResourceTypes, int~
    objects: HashMap~Objects, int~

    CountingObjective(HashMap~ResourceTypes,int~ resources, HashMap~ObjectsTypes,int~ objects)

    evaluate(HashMap~ResourceTypes,int~ resources, HashMap~ObjectsTypes,int~ objects) int
}

PlayedCard <-- CardSidesTypes : uses
PlayedCard *-- SidedCard: is composed of
GoldCard <-- PointConditionTypes : uses
CornerEnum <-- Corner : uses
CardSide "1"<|--"1" CardBackSide : inherits from
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
 

class Game {

    -tokens: Token[9]
    -players: Player[2..4]
    -gameBoard: GameBoard
    -state: GameStates[0..1]
    %% the scores key is the player's nickname
    -scores: HashMap~string, int~
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
    %% to be specified

    +evaluateObjectives()
    %% calls player.evaluateObjectives() for each player with the common objectives as parameter
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
    cards: List~T~
    %% Una pila forse
    Deck(List~T~ cards)
    shuffle() void
    draw() T ~~throws~~ EmptyDeckException
    draw(int N) List~T~ ~~throws~~ EmptyDeckException
    getCardsLeft() int
    %% insert card back after card drawing. (ie when you don't choose an objective)
    insert(T card) void
}

class Player {
    nickname: String
    points: int
    token: Token
    board: playerBoard
    Player(String nickname, Token token)
    getPoints() int
    setPoints(int) void
    %% points are abviously private


    +drawCard(SidedCard card) void
    %% receive  card and put it in the player's hand

    +chooseObjective(ObjectiveCard[2] availableObjectives) ObjectiveCard

    +chooseToken()

    placeCard(int cardNumber) void
    %% removes the card from the player's hand and places it on the board calling the playerboard method

    evaluateObjectives(ObjectiveCard[2] commonObjectives) void
}

class DrawingDeckTypes {
    <<Enumeration>>
    GOLD_DECK
    RESOURCE_DECK
}

class Token {
    color: TokenColors
    Token(TokenColors)
}

class Position{
    x: int
    y: int

    computeLinkingPosition(CornerEnum linkedCorner) Position
    Position(x,y)

    %% Overriding default hashmap key methods
    equals(Position) bool
    hashCode() int
}

class GameBoard {
    -goldDeck : Deck~GoldCard~
    -goldCards: CardPair~GoldCard~
    -starterDeck: Deck~StarterCard~
    -objectiveDeck: Deck~ObjectiveCard~
    -resourceDeck: Deck~ResourceCard~
    -resourceCards: CardPair~ResourceCard~
    -objectiveCards: CardPair~ObjectiveCard~
    
    %% the game board has two constructors, one with parameters and one without
    %% the constructor with parameters is used to restore a game from a save file
    GameBoard(CardPair~Goldcards~ goldCards, CardPair~ResourceCard~ resourceCards, \nCardPair~ObjectiveCard~ objectiveCards, \nDeck~StarterCard~ starterDeck, \nDeck~ObjectiveCard~ objectiveDeck, \nDeck~ResourceCard~ resourceDeck, \nDeck~GoldCard~ goldDeck)
    %% the constructor without parameters is used to create a new game
    GameBoard(List~GoldCard~ goldCardsList,\n \nList~StarterCard~ starterCardsList, \nList~ObjectiveCard~ objectiveCardsList, \nList~ResourceCard~ resourceCardsList)

    drawGoldCardFromDeck() GoldCard ~~throws~~ EmptyDeckException
    drawGoldCardFromPair(boolean first) GoldCard ~~throws~~ EmptyDeckException
    getGoldCards() CardPair~GoldCard~
    getGoldCardsLeft() int

    drawStarterCard() StarterCard ~~throws~~ EmptyDeckException
    getStarterCardsLeft() int
    
    drawObjectiveCardFromDeck() ObjectiveCard ~~throws~~ EmptyDeckException
    drawObjectiveCardFromPair(boolean first) ObjectiveCard ~~throws~~ EmptyDeckException
    getObjectiveCards() CardPair~ObjectiveCard~
    getObjectiveCardsLeft() int

    drawResourceCardFromDeck() ResourceCard ~~throws~~ EmptyDeckException
    drawResourceCardFromPair(boolean first) ResourceCard ~~throws~~ EmptyDeckException
    getResourceCards() CardPair~ResourceCard~
    getResourceCardsLeft() int

    
}

class EmptyDeckException {
    
}


class PlayerBoard {
    cards: SidedCard[3]
    objectiveCards: ObjectiveCard
    %% the geometry is an hasmap of positions and played cards

    playedCards: HashMap~Position, PlayedCard~
    AvailableSpots: List~Position~

    resources: HashMap~ResourceType, int~
    objects: HashMap~Objects, int~

    PlayerBoard(SidedCard[3] cards, ObjectiveCard objectiveCard, startCard)

    +chosePlacingPosition(int) Position

    placeCard(SidedCard card, cardSidesTypes side, Position position) void
    %% instanciates a played card and places it on the board, updates the resources and objects calling the helper method, updates the available spots calling the helper method

    updateResourcesandObjects(PlayedCard playedCard, Position position) void

    updateAvailableSpots(Position position) void

    evaluatePoints(PlayedCard card) int
}

Game "2"*--"4" Player : is composed of
Game "1"*--"1" GameBoard : is composed of
Game "1"*--"9" Token : is composed of
Token <-- TokenColors : uses
GameBoard "1"*--"4" Deck : is composed of

PlayerBoard <-- Position : uses
Player --|> Iterable : implements
GameBoard --|> EmptyDeckException : composition
Deck --|> EmptyDeckException : composition
Player --* PlayerBoard: composed of

Player <-- DrawingDeckTypes  : uses
GameBoard <-- CardPair: uses
```

## Considerations

The rationale is to implement every element that can become graphical as a separate class, so that there is a correspondence once the view is implemented. Each element will have a decorator toString to realize the cli and a method to draw it on the GUI
