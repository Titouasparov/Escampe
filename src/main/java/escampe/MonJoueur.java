package escampe;

import iialib.games.algs.algorithms.AlphaBeta;

// cette classe permet de définir un joueur pour le jeu d'Escampe
public class MonJoueur implements IJoueur {

    // --- Attributs ---
    private EscampeBoard board;
    private int maCouleurInt;
    private String maCouleurStr;
    private boolean aFaitSonPlacement = false;

    // L'Algorithme d'IA
    // <Coup, Role, Plateau>
    private AlphaBeta<EscampeMove, EscampeRole, EscampeBoard> algo;

    // --- Initialisation ---
    @Override
    public void initJoueur(int mycolour) {
        this.maCouleurInt = mycolour;
        this.maCouleurStr = (mycolour == IJoueur.BLANC) ? "blanc" : "noir";

        this.board = new EscampeBoard();
        this.aFaitSonPlacement = false;

        // Configuration de l'IA
        EscampeRole monRole = new EscampeRole(this.maCouleurStr);
        EscampeRole roleAdverse = new EscampeRole((mycolour == IJoueur.BLANC) ? "noir" : "blanc");

        // Création de l'algo AlphaBeta(RoleJoueur, RoleAdversaire, Heuristique, Profondeur)
        // PROFONDEUR : 4 est un bon début pour tester la rapidité.
        System.out.println(">>> Init IA AlphaBeta (Profondeur 4)...");
        this.algo = new AlphaBeta<>(monRole, roleAdverse, new EscampeHeuristique(), 4);
    }

    // --- GETTER ---
    @Override
    public int getNumJoueur() {
        return maCouleurInt;
    }

    @Override
    public String binoName() {
        return "Babic - Beauverger";
    }

    // --- Recevoir le coup adverse ---
    // Exemple de coup : "A1-B2" ou "PASSE" ou "E" (erreur)
    // permet de mettre à jour le plateau interne avec le coup joué par l'adversaire
    @Override
    public void mouvementEnnemi(String coup) {
        // Gérer le cas où l'adversaire passe ou erreur
        if (coup.equals("PASSE") || coup.equals("E")) {
            try {
                // On met à jour le tour dans le plateau interne
                String couleurAdverse = (this.maCouleurStr.equals("blanc")) ? "noir" : "blanc";
                this.board.play("E", couleurAdverse);//Mise à jour du plateau
            } catch(Exception e) {}
            return;
        }

        System.out.println(">>> Adversaire joue : " + coup);
        String couleurAdverse = (this.maCouleurStr.equals("blanc")) ? "noir" : "blanc";
        try {
            this.board.play(coup, couleurAdverse);//Mise à jour du plateau
        } catch (Exception e) {
            System.err.println("ERREUR CRITIQUE : Impossible de jouer le coup adverse (" + coup + ")");
            e.printStackTrace();
        }
    }

    // --- Choisir son mouvement ---
    // Permet de choisir le coup à jouer en utilisant l'IA AlphaBeta
    // Retourne le coup à jouer sous forme de String "A1-B2" ou "PASSE"
    @Override
    public String choixMouvement() {
        String coupAJouer = "";

        // Phase de Placement
        // L'IA AlphaBeta ne sait pas gérer le placement initial complexe ("A1/B2...")
        if (!aFaitSonPlacement) {
            coupAJouer = genererPlacement();
            this.aFaitSonPlacement = true;
        }
        // Phase de Jeu, notre IA entre en action
        else {
            try {
                // Appel à l'IA pour trouver le meilleur coup
                // bestMove renvoie un objet EscampeMove
                EscampeMove bestMove = this.algo.bestMove(this.board, new EscampeRole(this.maCouleurStr));

                if (bestMove != null) {
                    coupAJouer = bestMove.toString(); // Conversion en String "A1-B2"
                } else {
                    System.out.println(">>> ALERTE : IA n'a rien trouvé (bloqué ?), je tente un coup par défaut.");
                    // Fallback : jouer le premier coup possible si l'IA renvoie null
                    String[] coups = this.board.possiblesMoves(this.maCouleurStr);
                    if(coups.length > 0) coupAJouer = coups[0];
                    else coupAJouer = "E"; // Passe
                }
            } catch (Exception e) {
                System.err.println("ERREUR IA : " + e.getMessage());
                e.printStackTrace();
                coupAJouer = "E"; // Sécurité
            }
        }

        System.out.println(">>> Je joue : " + coupAJouer);

        // IMPORTANT : On joue le coup sur NOTRE plateau interne
        this.board.play(coupAJouer, this.maCouleurStr);

        return coupAJouer;
    }

    // --- Déclarer le vainqueur ---
    @Override
    public void declareLeVainqueur(int colour) {
        System.out.println("Fin. Vainqueur : " + (colour == maCouleurInt ? "NOUS" : "EUX"));
    }

    // --- Helper Placement ---
    // Génère une chaîne de placement initial en fonction de la couleur du joueur
    private String genererPlacement() {
        if (this.maCouleurStr.equals("noir")) {
            return "C6/A6/B6/D6/E6/F6";
        } else {
            return "C1/A1/B1/D1/E1/F1";
        }
    }
}