# Projet IA : Jeu Escampe

**Auteurs :** Marko BABIC & Titouan BEAUVERGER
**Rendu :** Partie 2 - Codage des opérations de base

## Description
Le projet fournit l'infrastructure nécessaire pour représenter l'état du jeu, valider les mouvements selon les règles spécifiques (liserés, distances, obstacles), générer les coups possibles et gérer la persistance (sauvegarde/chargement).

## Fonctionnalités Implémentées

Le cœur du projet réside dans la classe `EscampeBoard.java` qui implémente l'interface `Partiel` :

* **Gestion du Plateau :** Représentation interne matricielle ($6 \times 6$) et gestion statique des types de cases (liserés 1, 2, 3).
* **Entrées/Sorties (I/O) :**
    * Lecture (`setFromFile`) de fichiers de configuration `.txt`.
    * Écriture (`saveToFile`) de l'état courant du jeu.
* **Moteur de Règles :**
    * `isValidMove` : Vérification rigoureuse de la validité d'un coup (respect du liseré imposé, distance exacte, trajectoire libre, interdiction de diagonale, tir fratricide).
    * `play` : Exécution des coups, gestion des captures et mise à jour automatique du **liseré imposé** pour le tour suivant.
    * `possiblesMoves` : Algorithme de recherche (Pathfinding) pour générer tous les coups légaux, incluant les trajectoires complexes en "L" (virages autorisés).
    * `gameOver` : Détection de la fin de partie (disparition d'une licorne).

## Arborescence des Fichiers
```text
.
├── src/
│   └── main/
│       └── java/
│           ├── games/
│           │   └── escampe/
│           │       └── EscampeBoard.java   <-- Main
│           └── iialib/
│               └── model/
│                   └── Partiel.java        <-- Interface de l'énoncé
├── test_input1.txt
├── test_input2.txt
├── test_input3.txt
└── test_input4.txt 
```