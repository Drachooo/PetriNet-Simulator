# Petri Net Process Management Application

> **Corso:** Ingegneria del Software - Anno Accademico 2024-2025  
> **Progetto:** Simulatore di Reti di Petri con Controllo degli Accessi (RBAC)

## 📄 Abstract
Questo progetto implementa un sistema per la modellazione e l'esecuzione di processi distribuiti tramite **Reti di Petri**. Il sistema estende il modello classico introducendo un controllo degli accessi basato sui ruoli, distinguendo tra **Amministratori** (che progettano le reti) e **Utenti Finali** (che eseguono i processi).

## 🧩 Definizioni Formali
Il sistema rispetta la definizione matematica di una Rete di Petri come una quadrupla $N = (P, T, A, M_0)$ dove:
* **$P$**: Insieme finito di posti $\{p_1, p_2, ..., p_n\}$.
* **$T$**: Insieme finito di transizioni $\{t_1, t_2, ..., t_m\}$.
* **$A$**: Insieme di archi orientati (subset di $(P \times T) \cup (T \times P)$).
* **$M_0$**: Marcatura iniziale che assegna un numero non negativo di token a ciascun posto.

### Regole di Semantica
1.  **Abilitazione**: Una transizione $t$ è abilitata se ogni posto di input contiene almeno un token.
2.  **Scatto (Firing)**: L'esecuzione di una transizione rimuove un token da ogni posto di input e ne aggiunge uno a ogni posto di output.
3.  **Posti Speciali**:
    * **$p_{init}$**: Unico posto iniziale con 1 token, nessun arco entrante, almeno un arco uscente.
    * **$p_{final}$**: Unico posto finale, nessun arco uscente, almeno un arco entrante.

---

## 👥 Ruoli e Permessi

Il sistema gestisce due attori principali con permessi distinti.

### 1. Amministratore (Administrator)
* **Design**: Crea reti di Petri definendo posti, transizioni e archi.
* **Partizionamento**: Definisce quali transizioni sono di tipo "Admin" e quali di tipo "User".
* **Gestione**: Può visualizzare ed eliminare le computazioni relative alle proprie reti.
* **Esecuzione**: Può scattare *solo* le transizioni designate come "Administrator".
* **Restrizione**: Non può agire come utente (Subscriber) delle proprie reti.

### 2. Utente Finale (End User)
* **Sottoscrizione**: Può iscriversi a reti create da altri amministratori.
* **Istanziazione**: Avvia nuove computazioni (istanze di processo).
* **Esecuzione**: Può scattare *solo* le transizioni designate come "User".
* **Storico**: Visualizza la cronologia delle transizioni con timestamp.
* **Vincoli**: Massimo una computazione attiva per rete di Petri.

---

## ⚙️ Funzionalità Chiave

### Gestione del Ciclo di Vita del Processo
1.  **Avvio**: Il processo inizia con un token in $p_{init}$.
2.  **Collaborazione**: L'esecuzione alterna transizioni Admin e User. Se un utente non completa le sue transizioni, l'admin non può procedere (e viceversa).
3.  **Completamento**: Quando un token raggiunge $p_{final}$, la computazione è marcata automaticamente come completata.

### Requisiti di Sistema
* **Visualizzazione Grafica**: Mostra lo stato corrente (token nei posti).
* **Filtraggio Transizioni**: L'utente vede abilitate solo le transizioni pertinenti al proprio ruolo.
* **Log Cronologico**: Ogni step (transizione + marcatura risultante) è salvato con timestamp.

---

## 🗄️ Data Model

Il database supporta le seguenti entità logiche:

| Entità | Descrizione | Attributi Chiave |
| :--- | :--- | :--- |
| **PetriNet** | La definizione del processo | ID, Name, AdminID, $p_{init}$, $p_{final}$ |
| **Place** | I nodi stato del grafo | ID, PetriNetID, Name |
| **Transition** | I nodi evento del grafo | ID, PetriNetID, Type (Admin/User) |
| **Arc** | Connessioni orientate | SourceID, TargetID |
| **Computation** | Istanza di esecuzione | ID, UserID, PetriNetID, Status, Start/EndTime |
| **Comp.Step** | Log dello storico | TransitionID, Timestamp, MarkingData |

---

## 🚀 Use Case Principali

### Administrator
* **Create Petri Net**: Definizione della topologia e dei permessi.
* **Manage Computations**: Monitoraggio ed eliminazione forzata di istanze.

### End User
* **Subscribe**: Accesso a un processo esistente.
* **Start Computation**: Creazione di un nuovo token in $p_{init}$.
* **Execute Transition**: Scatto di una transizione disponibile.

---

## 🛠️ Requisiti Non Funzionali
* **Usabilità**: Interfaccia chiara per la visualizzazione delle transizioni abilitate.
* **Sicurezza**: Strict enforcement dei permessi (un utente non può mai scattare transizioni admin).
