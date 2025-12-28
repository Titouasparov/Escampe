package escampe;

import iialib.games.algs.algorithms.AlphaBetaDeepening;

public class JoueurDeepening implements IJoueur {

    // --- Attributs ---
    private EscampeBoard board;
    private int maCouleurInt;
    private String maCouleurStr;
    private boolean aFaitSonPlacement = false;

    // Utilisation de la nouvelle classe Deepening
    private AlphaBetaDeepening<EscampeMove, EscampeRole, EscampeBoard> algo;

    // Gestion du temps
    private long tempsRestantMs = 300000; // 5 minutes en millisecondes
    private static final int MAX_DEPTH_ABSOLUE = 10;

    // --- Initialisation ---
    @Override
    public void initJoueur(int mycolour) {
        this.maCouleurInt = mycolour;
        this.maCouleurStr = (mycolour == IJoueur.BLANC) ? "blanc" : "noir";

        this.board = new EscampeBoard();
        this.aFaitSonPlacement = false;

        EscampeRole monRole = new EscampeRole(this.maCouleurStr);
        EscampeRole roleAdverse = new EscampeRole((mycolour == IJoueur.BLANC) ? "noir" : "blanc");

        // Initialisation de l'algorithme avec Iterative Deepening
        System.out.println(">>> Init IA AlphaBeta avec Iterative Deepening...");
        this.algo = new AlphaBetaDeepening<>(monRole, roleAdverse, new EscampeHeuristique(), MAX_DEPTH_ABSOLUE);
    }

    @Override
    public int getNumJoueur() {
        return maCouleurInt;
    }

    @Override
    public String binoName() {
        return "Babic - Beauverger";
    }

    @Override
    public void mouvementEnnemi(String coup) {
        if (coup.equals("PASSE") || coup.equals("E")) {
            try {
                String couleurAdverse = (this.maCouleurStr.equals("blanc")) ? "noir" : "blanc";
                this.board.play("E", couleurAdverse);
            } catch(Exception e) {}
            return;
        }

        System.out.println(">>> Adversaire joue : " + coup);
        String couleurAdverse = (this.maCouleurStr.equals("blanc")) ? "noir" : "blanc";
        try {
            this.board.play(coup, couleurAdverse);
        } catch (Exception e) {
            System.err.println("ERREUR CRITIQUE : Coup adverse invalide (" + coup + ")");
        }
    }

    @Override
    public String choixMouvement() {
        String coupAJouer = "";

        // 1. Phase de Placement (Statique)
        if (!aFaitSonPlacement) {
            coupAJouer = genererPlacement();
            this.aFaitSonPlacement = true;
        }
        // 2. Phase de Jeu (IA avec gestion du temps)
        else {
            long startTime = System.currentTimeMillis();

            // Stratégie de gestion du temps :
            // On estime qu'il reste environ 30 coups à jouer.
            // On prend 1/30ème du temps restant pour ce coup.
            long budgetPourCeCoup = tempsRestantMs / 30;

            try {
                EscampeRole monRole = new EscampeRole(this.maCouleurStr);

                // Appel au Deepening
                EscampeMove bestMove = this.algo.bestMove(this.board, monRole, budgetPourCeCoup);

                if (bestMove != null) {
                    coupAJouer = bestMove.toString();
                } else {
                    // Sécurité si l'IA ne renvoie rien
                    String[] coups = this.board.possiblesMoves(this.maCouleurStr);
                    coupAJouer = (coups.length > 0) ? coups[0] : "E";
                }
            } catch (Exception e) {
                System.err.println("ERREUR IA : " + e.getMessage());
                coupAJouer = "E";
            }

            // Mise à jour du temps restant global
            long tempsEcoule = System.currentTimeMillis() - startTime;
            this.tempsRestantMs -= tempsEcoule;
            System.out.println(">>> Temps restant : " + (tempsRestantMs / 1000) + "s");
        }

        System.out.println(">>> Je joue : " + coupAJouer);
        this.board.play(coupAJouer, this.maCouleurStr);
        return coupAJouer;
    }

    @Override
    public void declareLeVainqueur(int colour) {
        System.out.println("Fin. Vainqueur : " + (colour == maCouleurInt ? "NOUS" : "EUX"));
    }

    private String genererPlacement() {
        return (this.maCouleurStr.equals("noir")) ? "C6/A6/B6/D6/E6/F6" : "C1/A1/B1/D1/E1/F1";
    }
}