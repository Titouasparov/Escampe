package games.ecampe;


import iialib.games.model.escampe.Partie1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class EscampeBoard implements Partie1 {

    // --- ATTRIBUTS STATIQUES (Le Plateau) ---

    // Représentation des liserés du plateau (1, 2, ou 3).
    // Note : L'orientation dépend de votre lecture (Ligne 0 = Haut ou Bas ?).
    // Ici, j'assume que l'indice 0 correspond à la ligne 1 (Bas) et l'indice 5 à la ligne 6 (Haut),
    // conformément à la logique "bas en haut" décrite pour les fichiers[cite: 462].
    // À VÉRIFIER VISUELLEMENT AVEC LE PLATEAU PHYSIQUE.
    private static final int[][] LISERES = {
            {1, 2, 2, 3, 1, 2}, // Ligne 1 (A1 -> F1)
            {3, 1, 3, 1, 3, 2}, // Ligne 2
            {2, 3, 1, 2, 1, 3}, // Ligne 3
            {2, 1, 3, 2, 3, 1}, // Ligne 4
            {1, 3, 1, 3, 1, 2}, // Ligne 5
            {3, 2, 2, 1, 3, 2}  // Ligne 6 (A6 -> F6)
    };

    // --- ATTRIBUTS DYNAMIQUES (L'État du jeu) ---

    // Position des pièces
    // 0 = vide
    // 1 = Paladin Blanc, 2 = Licorne Blanche
    // -1 = Paladin Noir, -2 = Licorne Noire
    private int[][] posPieces;

    // Le liseré imposé par le coup précédent (0 si aucun, 1, 2, ou 3)
    private int lisereCourant;

    // Le joueur qui doit jouer ("blanc" ou "noir")
    private String joueurCourant;

    // --- CONSTRUCTEUR ---

    public EscampeBoard() {
        // Initialisation d'un plateau vide 6x6
        this.posPieces = new int[6][6];
        this.lisereCourant = 0; // Pas de contrainte au début
        this.joueurCourant = "blanc"; // Par défaut, les blancs commencent (règle 2) [cite: 414]
    }

    // --- MÉTHODES DE L'INTERFACE PARTIEL ---

    @Override
    public void setFromFile(String fileName) {
        // TODO : Lire le fichier et remplir posPieces
        // Format : lignes indicées de bas en haut
        System.out.println("Lecture du fichier : " + fileName);
    }

    @Override
    public void saveToFile(String fileName) {
        // TODO : Écrire posPieces dans un fichier
        System.out.println("Sauvegarde vers : " + fileName);
    }

    @Override
    public boolean isValidMove(String move, String player) {
        // TODO : Vérifier syntaxe, liseré, distance, obstacles
        return false;
    }

    @Override
    public String[] possiblesMoves(String player) {
        // TODO : Générer tous les coups valides pour 'player'
        return new String[0];
    }

    @Override
    public void play(String move, String player) {
        // TODO : Mettre à jour posPieces et lisereCourant
        System.out.println("Joueur " + player + " joue " + move);
    }

    @Override
    public boolean gameOver() {
        // TODO : Vérifier la présence des deux licornes (2 et -2)
        return false;
    }

    // --- PROGRAMME PRINCIPAL ---

    public static void main(String[] args) {
        EscampeBoard board = new EscampeBoard();
        System.out.println("Plateau initialisé.");

        // Test simple d'affichage (temporaire)
        System.out.println("Liseré en A1 (0,0) : " + LISERES[0][0]);
    }
}
