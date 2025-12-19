package escampe;

public class MonJoueur implements IJoueur {

    // --- Attributs ---
    private EscampeBoard board;
    private int maCouleurInt;       // -1 pour Blanc, 1 pour Noir
    private String maCouleurStr;    // "blanc" ou "noir"
    private boolean aFaitSonPlacement = false; // Indique si le placement initial a été fait

    // --- Initialisation ---
    // définir la couleur du joueur et initialiser le plateau
    @Override
    public void initJoueur(int mycolour) {
        this.maCouleurInt = mycolour;
        this.maCouleurStr = (mycolour == IJoueur.BLANC) ? "blanc" : "noir";

        this.board = new EscampeBoard();
        this.aFaitSonPlacement = false;

        System.out.println(">>> Initialisation MonJoueur (" + maCouleurStr + ")");
    }

    @Override
    public int getNumJoueur() {
        return maCouleurInt;
    }

    // --- Nom du Binôme ---
    @Override
    public String binoName() {
        return "Babic - Beauverger"; // Nos noms de famille
    }

    // --- Recevoir le coup de l'adversaire ---
    @Override
    public void mouvementEnnemi(String coup) {
        // Si l'adversaire passe ou joue un coup spécial non géré
        if (coup.equals("PASSE") || coup.equals("E")) {
            System.out.println(">>> L'adversaire passe son tour.");
            // On doit juste changer le tour dans notre plateau interne si nécessaire
            // board.play("E", couleurAdverse);
            return;
        }

        System.out.println(">>> Adversaire joue : " + coup);
        String couleurAdverse = (this.maCouleurStr.equals("blanc")) ? "noir" : "blanc";

        // Mise à jour du plateau local
        try {
            this.board.play(coup, couleurAdverse);
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du coup adverse : " + e.getMessage());
        }
    }

    // --- Choisir son mouvement ---
    @Override
    public String choixMouvement() {
        String coupAJouer = "";

        // CAS 1 : PHASE DE PLACEMENT
        if (!aFaitSonPlacement) {
            coupAJouer = genererPlacement();
            this.aFaitSonPlacement = true;
        }
        // CAS 2 : PHASE DE JEU
        // On doit choisir un coup à jouer

        else {
            // Stratégie temporaire : Premier coup valide
            String[] coupsPossibles = this.board.possiblesMoves(this.maCouleurStr);

            if (coupsPossibles.length > 0) {
                coupAJouer = coupsPossibles[0];
            } else {
                coupAJouer = "E"; // "E" pour passer son tour (convention Escampe)
            }
        }

        // Jouer le coup sur notre plateau AVANT de l'envoyer
        System.out.println(">>> Je joue : " + coupAJouer);
        try {
            this.board.play(coupAJouer, this.maCouleurStr);
        } catch (Exception e) {
            System.err.println("Erreur lors de mon propre coup : " + e.getMessage());
            return "E"; // Sécurité
        }

        return coupAJouer;
    }
    // --- Déclarer le vainqueur ---
    @Override
    public void declareLeVainqueur(int colour) {
        System.out.println("Fin de partie. Vainqueur : " + (colour == maCouleurInt ? "NOUS" : "EUX"));
    }

    // --- Helper Placement ---
    // Permet de générer une chaîne de placement initial en fonction de la couleur
    private String genererPlacement() {
        if (this.maCouleurStr.equals("noir")) {
            return "C6/A6/B6/D6/E6/F6";
        } else {
            return "C1/A1/B1/D1/E1/F1";
        }
    }
}