- implementare la view come interfaccia, le realizzazioni saranno TUI and GUI

- virtual view 

- RMI un poco diverso. gira come se fosse in locale

- login lo deve implementare il modello ? o il server

- non mandare oggetti che corrispondono al modello: creare view limitate intasiamo la rete con gli oggetti. il client ha bisogno di poche informazioni:
bisogna creare il protocollo



unico punto in cui si può fare instanceof è quando ricevo un oggetto serializzato


telnet e netcat per testare la connessione

+ per il client sarebbe comodo il pattern state
funzioni che vengono overridate in base allo stato cioè playcard risponderà in base allo stato del giocatore se per esempio è playing o waiting