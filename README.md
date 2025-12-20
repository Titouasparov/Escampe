# Projet IA : Jeu Escampe

**Auteurs :** Marko BABIC & Titouan BEAUVERGER
**Rendu :** Partie 3 - Joueur IA & Connexion Réseau

## Description
Ce projet implémente une Intelligence Artificielle pour le jeu de stratégie **Escampe**.
Il fournit l'infrastructure complète pour :
1.  **Gérer le jeu :** Représentation du plateau, validation des règles, génération des coups.
2.  **Raisonner :** Une IA utilisant l'algorithme **AlphaBeta** avec une heuristique de mobilité.
3.  **Communiquer :** Une couche réseau permettant à l'IA de se connecter au serveur officiel du tournoi.

## Fonctionnalités Implémentées

### 1. Moteur de Jeu (`EscampeBoard.java`)
* **Gestion du Plateau :** Représentation interne matricielle ($6 \times 6$) et gestion des liserés (1, 2, 3).
* **Moteur de Règles :**
  * `isValidMove` : Vérification rigoureuse (liseré imposé, distance, obstacles, tir fratricide).
  * `play` : Exécution des coups et mise à jour automatique du **liseré imposé**.
  * `possiblesMoves` : Génération de tous les coups légaux (pathfinding).
  * `gameOver` : Détection de la fin de partie (capture de licorne).

### 2. Intelligence Artificielle (`MonJoueur.java`)
* **Algorithme :** Implémentation de **AlphaBeta** (élagage) pour optimiser la recherche.
* **Heuristique :** Évaluation basée sur la **mobilité** (différence entre mon nombre de coups possibles et celui de l'adversaire).
* **Adaptateurs :** Classes `EscampeMove` et `EscampeRole` pour faire le lien entre le jeu (Strings) et la librairie d'IA générique.

### 3. Connexion Réseau
* Implémentation de l'interface `escampe.IJoueur`.
* Communication via Sockets avec le serveur `escampeobf.jar`.

## Comment lancer une partie (Tournoi)

Pour faire jouer l'IA, vous devez ouvrir **3 terminaux** distincts à la racine du projet.

**Terminal 1 : Lancer le Serveur**
```bash
java -cp escampeobf.jar escampe.ServeurJeu 1234 1
```
**Terminal 2 : Lancer le Joueur 1 (Notre IA) (Sur Windows, utilisez ; comme séparateur. Sur Mac/Linux, utilisez :)**
```bash
java -cp "build/classes/java/main;escampeobf.jar" escampe.ClientJeu escampe.MonJoueur localhost 1234
```
**Terminal 3 : Lancer le Joueur 2 (Notre IA ou Adversaire)**
```bash
java -cp "build/classes/java/main;escampeobf.jar" escampe.ClientJeu escampe.MonJoueur localhost 1234
```

## Arborescence des Fichiers
```text
.
├── escampeobf.jar                  <-- Serveur du tournoi fournie
├── build.gradle                    <-- Configuration Gradle
├── src/
│   └── main/
│       └── java/
│           ├── escampe/            <-- Package Principal
│           │   ├── Applet.java
│           │   ├── ClientJeu.java
│           │   ├── EscampeBoard.java       <-- Moteur du jeu (Compatible IBoard)
│           │   ├── EscampeHeuristique.java <-- Cerveau de l'IA (Evaluation)
│           │   ├── EscampeMove.java        <-- Adaptateur Coup
│           │   ├── EscampeRole.java        <-- Adaptateur Joueur
│           │   ├── IJoueur.java            <-- Interface Tournoi
│           │   ├── MonJoueur.java          <-- Notre IA (AlphaBeta)
│           │   └── Solo.java
│           └── iialib/             <-- Librairie IA (Fournie dans les TP precedents)
│               ├── games/
│               │   ├── algs/       <-- Algorithmes (AlphaBeta, Minimax...)
│               │   └── model/      <-- Interfaces (IBoard, IMove...)
├── test_input1.txt <-- Fichier test de EscampeBoard
├── test_input2.txt <-- Fichier test de EscampeBoard
├── test_input3.txt <-- Fichier test de EscampeBoard
└── test_input4.txt <-- Fichier test de EscampeBoard
```