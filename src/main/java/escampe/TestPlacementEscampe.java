package escampe;

import java.util.ArrayList;
import java.util.List;

public class TestPlacementEscampe {

    // --- CARTE DES LISERÉS ---
    // 1 = Liseré Simple, 2 = Double, 3 = Triple
    // Représentation : board[row][col] avec row 0..5 (bas->haut) et col 0..5 (A->F)
    static final int[][] LISERES = {
            {1, 2, 2, 3, 1, 2}, // Ligne 1 (Indice 0) - Zone Blanc
            {3, 1, 3, 1, 3, 2}, // Ligne 2 (Indice 1) - Zone Blanc
            {2, 3, 1, 2, 1, 3}, // Ligne 3 (Indice 2)
            {2, 1, 3, 2, 3, 1}, // Ligne 4 (Indice 3)
            {1, 3, 1, 3, 1, 2}, // Ligne 5 (Indice 4) - Zone Noir
            {3, 2, 2, 1, 3, 2}  // Ligne 6 (Indice 5) - Zone Noir
    };

    public static void main(String[] args) {
        System.out.println("--- Recherche du Meilleur Placement Stratégique ---");

        // Générer tous les placements possibles du joueur Noir
        // Exemple de placement : "A5/B5/C6/D4/E4/F6" (Licorne en A5)
        // Ou bien : "C4/A5/B6/D5/E4/F6" (Licorne en C4)
        long start = System.currentTimeMillis();
        List<String> placements = genererTousPlacements(true); // true = Joueur Noir (Haut)

        // Évaluer chaque placement et trouver le meilleur
        // Pour cela on utilise la fonction evaluerPlacement définie ci-dessous
        String meilleurPlacement = "";
        double meilleurScore = Double.NEGATIVE_INFINITY;

        for (String p : placements) {
            double score = evaluerPlacement(p);
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurPlacement = p;
            }
        }
        long end = System.currentTimeMillis();

        System.out.println("Positions analysées : " + placements.size());
        System.out.println("Temps de calcul : " + (end - start) + " ms");
        System.out.println("-------------------------------------------------");
        System.out.println("MEILLEUR PLACEMENT TROUVÉ : " + meilleurPlacement);
        System.out.println("Score stratégique : " + meilleurScore);

        afficherAnalyse(meilleurPlacement);
    }

    /**
     * Évalue la qualité d'un placement initial.
     */
    public static double evaluerPlacement(String placement) {
        double score = 0;
        String[] pieces = placement.split("/");

        // Compteurs pour la diversité
        // On compte combien de pièces de chaque type de liseré on a
        int nbL1 = 0, nbL2 = 0, nbL3 = 0;

        // Analyse de chaque type pièce
        // pieces[0] = Licorne, pieces[1..5] = Paladins
        for (int i = 0; i < pieces.length; i++) {
            String coord = pieces[i];
            int col = coord.charAt(0) - 'A';
            int row = Character.getNumericValue(coord.charAt(1)) - 1;
            int typeLisere = LISERES[row][col];
            boolean isLicorne = (i == 0); // La première pièce est la Licorne par convention ici

            // Mise à jour des compteurs de liserés
            if (typeLisere == 1) nbL1++;
            else if (typeLisere == 2) nbL2++;
            else if (typeLisere == 3) nbL3++;

            // Bonus de Centralité (Colonnes C et D valent plus cher)
            // Pourquoi ? Car elles sont moins exposées aux snipers adverses.
            if (col == 2 || col == 3) {
                score += 2.0; // Bonus centre
            } else if (col == 1 || col == 4) {
                score += 1.0; // Bonus intermédiaire
            }

            // Stratégie spécifique à la Licorne
            if (isLicorne) {
                // On préfère la licorne sur un 3 (plus dur à sniper) ou un 1.
                // Le 2 est souvent très exposé.
                if (typeLisere == 3) score += 15.0;
                else if (typeLisere == 1) score += 5.0;

                // On évite de mettre la licorne sur les bords absolus (A ou F)
                if (col == 0 || col == 5) score -= 5.0;
            }
        }

        // DIVERSITÉ
        // Si on a au moins une pièce de chaque type, c'est très fort.
        // Sinon, c'est très dangereux (risque de blocage).
        if (nbL1 > 0 && nbL2 > 0 && nbL3 > 0) {
            score += 100.0; // Critère très valorisé
        } else {
            score -= 50.0; // Pénalité si on manque de diversité
        }

        // Bonus pour une distribution équilibrée (ex: 2-2-2 est mieux que 4-1-1)
        // car cela réduit les risques de blocage futur.
        // On retire l'écart type
        double ecart = Math.abs(nbL1 - 2) + Math.abs(nbL2 - 2) + Math.abs(nbL3 - 2);
        score -= ecart * 2.0;

        return score;
    }

    // --- AFFICHAGE  ---

    private static void afficherAnalyse(String placement) {
        System.out.println("\n--- Analyse du choix ---");
        String[] pieces = placement.split("/");
        int[] counts = new int[4]; // indices 1,2,3 utilisés

        System.out.print("Licorne en " + pieces[0] + " (Liseré " + getLisere(pieces[0]) + ")");
        counts[getLisere(pieces[0])]++;

        System.out.print(" | Paladins : ");
        for(int i=1; i<pieces.length; i++) {
            int l = getLisere(pieces[i]);
            System.out.print(pieces[i] + "(" + l + ") ");
            counts[l]++;
        }
        System.out.println("\nRépartition : L1=" + counts[1] + ", L2=" + counts[2] + ", L3=" + counts[3]);
        if(counts[1]>0 && counts[2]>0 && counts[3]>0) System.out.println(">> Diversité : OK (Risque de blocage faible)");
        else System.out.println(">> DANGER : Diversité incomplète !");
    }

    private static int getLisere(String coord) {
        int col = coord.charAt(0) - 'A';
        int row = Character.getNumericValue(coord.charAt(1)) - 1;
        return LISERES[row][col];
    }

    // --- CODE DE GÉNÉRATION ---
    // Génère tous les placements possibles pour un joueur (licorne + 5 paladins)
    public static List<String> genererTousPlacements(boolean isTopZone) {
        List<String> resultats = new ArrayList<>();
        List<Integer> casesDisponibles = new ArrayList<>();
        int startRow = isTopZone ? 4 : 0;
        for (int r = startRow; r < startRow + 2; r++) {
            for (int c = 0; c < 6; c++) {
                casesDisponibles.add(r * 6 + c);
            }
        }
        List<List<Integer>> combinaisonsCases = new ArrayList<>();
        combiner(casesDisponibles, 6, 0, new ArrayList<>(), combinaisonsCases);

        for (List<Integer> choixCases : combinaisonsCases) {
            for (int i = 0; i < choixCases.size(); i++) {
                Integer caseLicorne = choixCases.get(i);
                List<Integer> casesPaladins = new ArrayList<>(choixCases);
                casesPaladins.remove(i);
                resultats.add(construireStringPlacement(caseLicorne, casesPaladins));
            }
        }
        return resultats;
    }

    // Génère toutes les combinaisons de k éléments parmi une liste donnée
    // Concretement utilisée pour choisir 6 cases parmi 12
    // (2 lignes de 6 cases dans la zone de placement)
    private static void combiner(List<Integer> pool, int k, int startParams, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = startParams; i < pool.size(); i++) {
            current.add(pool.get(i));
            combiner(pool, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // Construit la chaîne de caractères représentant le placement
    private static String construireStringPlacement(Integer licorne, List<Integer> paladins) {
        StringBuilder sb = new StringBuilder();
        sb.append(indexToCoord(licorne));
        for (Integer p : paladins) {
            sb.append("/");
            sb.append(indexToCoord(p));
        }
        return sb.toString();
    }

    // Convertit un index (0..11) en coordonnée (ex: 0 -> A1, 5 -> F1, 6 -> A2, 11 -> F2)
    private static String indexToCoord(int index) {
        int r = index / 6;
        int c = index % 6;
        char colChar = (char)('A' + c);
        int rowNum = r + 1;
        return "" + colChar + rowNum;
    }
}