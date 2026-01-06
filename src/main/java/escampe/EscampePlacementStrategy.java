package escampe;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire dédiée uniquement au calcul du placement initial.
 * Elle ne garde aucun état (tout est static).
 */
public class EscampePlacementStrategy {

    // Le coup pré-calculé pour le joueur NOIR (Zone Haut).
    private static final String PLACEMENT_OPTIMAL_NOIR = "B5/C5/D5/B6/C6/D6";

    /**
     * Point d'entrée principal pour obtenir le placement de son joueur.
     */
    public static String getPlacement(String couleurJoueur) {
        if (couleurJoueur.equalsIgnoreCase("noir")) {
            System.out.println(">>> [Placement] Noir : On utilise notre placement optimal.");
            return PLACEMENT_OPTIMAL_NOIR;
        } else {
            System.out.println(">>> [Placement] Blanc : Calcul de la meilleure configuration...");
            // false = Zone du bas (Lignes 1-2 pour Blanc)
            return calculerMeilleurPlacement(false);
        }
    }

    // Calcule le meilleur placement en évaluant tous les candidats.
    // Concretement, génère tous les placements possibles du joueur de couleur donnée,
    private static String calculerMeilleurPlacement(boolean isTopZone) {
        List<String> candidats = genererTousPlacements(isTopZone);

        String meilleur = "";
        double maxScore = Double.NEGATIVE_INFINITY;

        for (String p : candidats) {
            double score = evaluerPlacement(p);
            if (score > maxScore) {
                maxScore = score;
                meilleur = p;
            }
        }
        return meilleur;
    }

    // Permet d'évaluer un placement initial selon une heuristique.
    // On a choisi plusieurs critères :
    // - Bonus pour les pièces au centre (colonnes C et D)
    // - Bonus pour la licorne sur un liseré triple
    // - Bonus pour la diversité des liserés
    // - Malus pour la licorne sur les bords (colonnes A et F)
    // Retourne un score double.
    private static double evaluerPlacement(String placement) {
        double score = 0;
        String[] pieces = placement.split("/");
        int nbL1 = 0, nbL2 = 0, nbL3 = 0;

        for (int i = 0; i < pieces.length; i++) {
            String coord = pieces[i];
            int col = coord.charAt(0) - 'A';
            int rowChar = Character.getNumericValue(coord.charAt(1)); // '1'..'6'

            // ATTENTION : Conversion coordonnée (1-6) vers indice tableau EscampeBoard
            // Dans EscampeBoard, souvent :
            // Ligne 6 (Haut) -> indice 0
            // Ligne 1 (Bas)  -> indice 5
            int rowIndex = 6 - rowChar;

            // On récupère le liseré depuis la classe EscampeBoard directement
            int typeLisere = EscampeBoard.LISERES[rowIndex][col];

            boolean isLicorne = (i == 0);

            if (typeLisere == 1) nbL1++;
            else if (typeLisere == 2) nbL2++;
            else if (typeLisere == 3) nbL3++;

            // Bonus Centre (C, D)
            if (col == 2 || col == 3) score += 2.0;
            else if (col == 1 || col == 4) score += 1.0;

            if (isLicorne) {
                if (typeLisere == 3) score += 15.0;
                else if (typeLisere == 1) score += 5.0;
                if (col == 0 || col == 5) score -= 5.0;
            }
        }

        // Diversité
        if (nbL1 > 0 && nbL2 > 0 && nbL3 > 0) score += 100.0;
        else score -= 50.0;

        return score;
    }

    // --- Combinatoire Pure ---

    // Génère tous les placements possibles pour un joueur dans sa zone.
    // isTopZone = true pour Noir (lignes 5-6), false pour Blanc (lignes 1-2).
    // Retourne une liste de Strings de placements valides.
    private static List<String> genererTousPlacements(boolean isTopZone) {
        List<String> resultats = new ArrayList<>();
        List<Integer> casesDisponibles = new ArrayList<>();

        // Logique indices :
        // Si TopZone (Noir, Lignes 5-6) -> Indices tableau 0 et 1 (car ligne 6 est en haut du tableau)
        // Si BotZone (Blanc, Lignes 1-2) -> Indices tableau 4 et 5
        // Attention : Cela dépend de l'ordre de déclaration dans EscampeBoard.LISERES.
        // On va assumer la logique "mathématique" des coordonnées 1..6 pour générer les Strings

        int startLigne = isTopZone ? 5 : 1; // 5 pour lignes 5-6, 1 pour lignes 1-2

        for (int r = startLigne; r < startLigne + 2; r++) {
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

    // Génère toutes les combinaisons de k éléments parmi une liste donnée.
    // Concrètement utilisée pour choisir 6 cases parmi 12.
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

    // Construit la chaîne de placement à partir des indices des cases.
    // La licorne est la première, suivie des paladins séparés par des "/".
    private static String construireStringPlacement(Integer licorne, List<Integer> paladins) {
        StringBuilder sb = new StringBuilder();
        sb.append(indexToCoord(licorne));
        for (Integer p : paladins) {
            sb.append("/");
            sb.append(indexToCoord(p));
        }
        return sb.toString();
    }

    // Convertit un index (0..11) en coordonnée (ex: 0 -> A1, 5 -> F1, 6 -> A2, 11 -> F2).
    private static String indexToCoord(int index) {
        // index est (ligne * 6 + col) où ligne est 1..6 (réel) ou 0..5 (tableau) ?
        // Dans genererTousPlacements, on a utilisé r * 6 + c avec r = 1..2 ou 5..6
        int r = index / 6;
        int c = index % 6;
        char colChar = (char)('A' + c);
        // r est déjà le numéro de ligne "réel" (1 à 6) grace à la boucle for
        return "" + colChar + r;
    }
}