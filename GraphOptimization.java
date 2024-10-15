import java.util.*;
import java.util.concurrent.TimeUnit;

public class GraphOptimization {
    private static Map<String, Integer> nodes = new HashMap<>(); // تخزين العقد مع الأوزان
    private static Map<List<String>, Integer> edges = new HashMap<>(); // تخزين الحواف مع الأوزان
    private static int L = 0; // الحد الأدنى لوزن الكلستر
    private static int U = 0; // الحد الأقصى لوزن الكلستر

    private static List<Integer> scoresGreedy = new ArrayList<>();
    private static List<Double> timesGreedy = new ArrayList<>();
    private static List<Integer> scoresLocal = new ArrayList<>();
    private static List<Double> timesLocal = new ArrayList<>();
    private static List<Integer> scoresAnnealing = new ArrayList<>();
    private static List<Double> timesAnnealing = new ArrayList<>();

    public static void setNodes(Map<String, Integer> newNodes) {
        nodes = newNodes;
    }

    public static void setEdges(Map<List<String>, Integer> newEdges) {
        edges = newEdges;
    }

    public static void setLimits(int lowerLimit, int upperLimit) {
        L = lowerLimit;
        U = upperLimit;
    }

    public static String runAndDisplayResults(int iterations, int runs) {
        scoresGreedy.clear();
        timesGreedy.clear();
        scoresLocal.clear();
        timesLocal.clear();
        scoresAnnealing.clear();
        timesAnnealing.clear();

        StringBuilder resultDetails = new StringBuilder();

        resultDetails.append("\n--- Greedy Heuristic ---\n");
        double[] greedyResults = executeAndDisplay("Greedy Heuristic", iterations, runs, scoresGreedy, timesGreedy, resultDetails);

        resultDetails.append("\n--- Local Search ---\n");
        double[] localSearchResults = executeAndDisplay("Local Search", iterations, runs, scoresLocal, timesLocal, resultDetails);

        resultDetails.append("\n--- Simulated Annealing ---\n");
        double[] simulatedAnnealingResults = executeAndDisplay("Simulated Annealing", iterations, runs, scoresAnnealing, timesAnnealing, resultDetails);

        resultDetails.append("\n--- Averaged Results over 10 executions ---\n");
        printAveragedResults("Greedy Heuristic", greedyResults, resultDetails);
        printAveragedResults("Local Search", localSearchResults, resultDetails);
        printAveragedResults("Simulated Annealing", simulatedAnnealingResults, resultDetails);

        resultDetails.append(displayClusterDetails());

        return resultDetails.toString();
    }

    private static double[] executeAndDisplay(String algorithm, int iterations, int runs, List<Integer> scores, List<Double> times, StringBuilder resultDetails) {
        for (int run = 0; run < runs; run++) {
            List<List<String>> initialClusters = greedyHeuristic();
            long startTime = System.nanoTime();

            int score;
            if (algorithm.equals("Greedy Heuristic")) {
                score = calculateScore(initialClusters);
            } else if (algorithm.equals("Local Search")) {
                score = localSearch(initialClusters, iterations)[0];
            } else if (algorithm.equals("Simulated Annealing")) {
                score = simulatedAnnealing(initialClusters, iterations)[0];
            } else {
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
            }

            long endTime = System.nanoTime();
            double elapsedTime = (endTime - startTime) / 1_000_000_000.0;

            scores.add(score);
            times.add(elapsedTime);

            resultDetails.append(String.format("Run %d - Score: %d, Time: %.6f sec\n", run + 1, score, elapsedTime));
        }

        double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double stdDevScore = calculateStdDev(scores, avgScore);
        double avgTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        int bestScore = scores.stream().max(Integer::compare).orElse(0);

        return new double[]{bestScore, avgScore, stdDevScore, avgTime};
    }

    public static List<Integer> getScoresListForGreedy() {
        return scoresGreedy;
    }

    public static List<Double> getTimesListForGreedy() {
        return timesGreedy;
    }

    public static List<Integer> getScoresListForLocal() {
        return scoresLocal;
    }

    public static List<Double> getTimesListForLocal() {
        return timesLocal;
    }

    public static List<Integer> getScoresListForAnnealing() {
        return scoresAnnealing;
    }

    public static List<Double> getTimesListForAnnealing() {
        return timesAnnealing;
    }

    private static void printAveragedResults(String algorithm, double[] results, StringBuilder resultDetails) {
        resultDetails.append(String.format("\n%s - Final Averaged Results:\n", algorithm));
        resultDetails.append(String.format("Best Score: %.0f\n", results[0]));
        resultDetails.append(String.format("Average Score: %.2f\n", results[1]));
        resultDetails.append(String.format("Standard Deviation: %.2f\n", results[2]));
        resultDetails.append(String.format("Average Computation Time: %.6f sec\n", results[3]));
    }

    private static String displayClusterDetails() {
        StringBuilder clusterDetails = new StringBuilder();
        List<List<String>> clusters = greedyHeuristic();

        int totalScore = 0;
        StringBuilder totalScoreEquation = new StringBuilder();
        
        clusterDetails.append("\n‒ Resulting Clusters:\n");
        for (int i = 0; i < clusters.size(); i++) {
            List<String> cluster = clusters.get(i);
            int totalWeight = calculateWeights(Collections.singletonList(cluster)).get(0);
            clusterDetails.append(String.format("• C%d = %s, total weight = %d\n", i + 1, cluster, totalWeight));

            StringBuilder clusterEdgeDetails = new StringBuilder();
            int clusterEdgeWeightSum = 0;
            for (int j = 0; j < cluster.size(); j++) {
                for (int k = j + 1; k < cluster.size(); k++) {
                    List<String> edge1 = Arrays.asList(cluster.get(j), cluster.get(k));
                    List<String> edge2 = Arrays.asList(cluster.get(k), cluster.get(j));
                    
                    int edgeWeight = 0;
                    if (edges.containsKey(edge1)) {
                        edgeWeight = edges.get(edge1);
                    } else if (edges.containsKey(edge2)) {
                        edgeWeight = edges.get(edge2);
                    }

                    if (edgeWeight > 0) {
                        clusterEdgeDetails.append(String.format("c%s%s = %d, ", cluster.get(j), cluster.get(k), edgeWeight));
                        clusterEdgeWeightSum += edgeWeight;
                        totalScoreEquation.append(edgeWeight).append(" + ");
                    }
                }
            }

            if (clusterEdgeDetails.length() > 2) {
                clusterEdgeDetails.setLength(clusterEdgeDetails.length() - 2);
            }
            clusterDetails.append(String.format("  ‒ Sum of edge weights within C%d: %s\n", i + 1, 
                           clusterEdgeDetails.length() > 0 ? clusterEdgeDetails.toString() : "None"));
            totalScore += clusterEdgeWeightSum;
        }

        if (totalScoreEquation.length() > 3) {
            totalScoreEquation.setLength(totalScoreEquation.length() - 3);
        }
        clusterDetails.append(String.format("Total score: %s = %d\n", totalScoreEquation.toString(), totalScore));
        return clusterDetails.toString();
    }

    private static int calculateScore(List<List<String>> clusters) {
        int score = 0;
        for (List<String> cluster : clusters) {
            for (int i = 0; i < cluster.size(); i++) {
                for (int j = i + 1; j < cluster.size(); j++) {
                    List<String> edge = Arrays.asList(cluster.get(i), cluster.get(j));
                    if (edges.containsKey(edge) || edges.containsKey(Arrays.asList(cluster.get(j), cluster.get(i)))) {
                        score += edges.getOrDefault(edge, 0) + edges.getOrDefault(Arrays.asList(cluster.get(j), cluster.get(i)), 0);
                    }
                }
            }
        }
        return score;
    }

    private static List<List<String>> greedyHeuristic() {
        List<List<String>> clusters = new ArrayList<>(Arrays.asList(new ArrayList<>(), new ArrayList<>()));
        List<Integer> clusterWeights = new ArrayList<>(Arrays.asList(0, 0));

        List<Map.Entry<String, Integer>> sortedNodes = new ArrayList<>(nodes.entrySet());
        sortedNodes.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<String, Integer> entry : sortedNodes) {
            String node = entry.getKey();
            int weight = entry.getValue();

            for (int i = 0; i < clusters.size(); i++) {
                if (clusterWeights.get(i) + weight <= U && (clusterWeights.get(i) + weight >= L || clusters.get(i).isEmpty())) {
                    clusters.get(i).add(node);
                    clusterWeights.set(i, clusterWeights.get(i) + weight);
                    break;
                }
            }
        }
        return clusters;
    }

    private static int[] localSearch(List<List<String>> initialClusters, int iterations) {
        List<List<String>> bestClusters = initialClusters;
        int bestScore = calculateScore(bestClusters);

        for (int i = 0; i < iterations; i++) {
            List<List<String>> newClusters = modifyClusters(bestClusters);
            int newScore = calculateScore(newClusters);

            if (newScore > bestScore) {
                bestScore = newScore;
                bestClusters = newClusters;
            }
        }
        return new int[]{bestScore};
    }

    private static int[] simulatedAnnealing(List<List<String>> initialClusters, int iterations) {
        List<List<String>> currentClusters = initialClusters;
        int currentScore = calculateScore(currentClusters);
        int bestScore = currentScore;
        double temperature = 1000;

        for (int i = 0; i < iterations; i++) {
            List<List<String>> newClusters = modifyClusters(currentClusters);
            int newScore = calculateScore(newClusters);

            if (newScore > currentScore || Math.exp((newScore - currentScore) / temperature) > Math.random()) {
                currentClusters = newClusters;
                currentScore = newScore;

                if (currentScore > bestScore) {
                    bestScore = currentScore;
                }
            }
            temperature *= 0.95;
        }
        return new int[]{bestScore};
    }

    private static List<List<String>> modifyClusters(List<List<String>> clusters) {
        Random random = new Random();
        List<List<String>> newClusters = new ArrayList<>();

        for (List<String> cluster : clusters) {
            newClusters.add(new ArrayList<>(cluster));
        }

        int cluster1Idx = random.nextInt(newClusters.size());
        int cluster2Idx = random.nextInt(newClusters.size());

        List<String> cluster1 = newClusters.get(cluster1Idx);
        List<String> cluster2 = newClusters.get(cluster2Idx);

        if (!cluster1.isEmpty() && !cluster2.isEmpty() && !cluster1.equals(cluster2)) {
            String node1 = cluster1.get(random.nextInt(cluster1.size()));
            String node2 = cluster2.get(random.nextInt(cluster2.size()));

            cluster1.remove(node1);
            cluster2.remove(node2);
            cluster1.add(node2);
            cluster2.add(node1);
        }

        List<Integer> weights = calculateWeights(newClusters);
        if (weights.stream().allMatch(weight -> weight >= L && weight <= U)) {
            return newClusters;
        }
        return clusters;
    }

    private static List<Integer> calculateWeights(List<List<String>> clusters) {
        List<Integer> weights = new ArrayList<>();
        for (List<String> cluster : clusters) {
            int weight = 0;
            for (String node : cluster) {
                weight += nodes.getOrDefault(node, 0);
            }
            weights.add(weight);
        }
        return weights;
    }

    private static double calculateStdDev(List<Integer> values, double mean) {
        double variance = values.stream()
                .mapToDouble(score -> Math.pow(score - mean, 2))
                .average().orElse(0.0);
        return Math.sqrt(variance);
    }
}

