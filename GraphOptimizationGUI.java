import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class GraphOptimizationGUI extends JFrame {
    private JTextField nodeNameField, nodeWeightField, edgeNode1Field, edgeNode2Field, edgeWeightField;
    private JTextField lowerLimitField, upperLimitField;
    private JTextArea resultsArea;
    private JPanel graphPanel;
    private Map<String, Integer> nodes = new HashMap<>();
    private Map<List<String>, Integer> edges = new HashMap<>();
    private Map<String, Point> nodePositions = new HashMap<>();

    public GraphOptimizationGUI() {
        setTitle("Graph Optimization");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.DARK_GRAY);
        inputPanel.setPreferredSize(new Dimension(200, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(createLabelRow("Node Name:", nodeNameField = new JTextField(10)));
        inputPanel.add(createLabelRow("Node Weight:", nodeWeightField = new JTextField(10)));
        inputPanel.add(createButtonRow("Add Node", e -> addNode()));

        inputPanel.add(createLabelRow("Edge Node 1:", edgeNode1Field = new JTextField(10)));
        inputPanel.add(createLabelRow("Edge Node 2:", edgeNode2Field = new JTextField(10)));
        inputPanel.add(createLabelRow("Edge Weight:", edgeWeightField = new JTextField(10)));
        inputPanel.add(createButtonRow("Add Edge", e -> addEdge()));

        inputPanel.add(createLabelRow("Lower Limit (L):", lowerLimitField = new JTextField(10)));
        inputPanel.add(createLabelRow("Upper Limit (U):", upperLimitField = new JTextField(10)));
        inputPanel.add(createButtonRow("Set Limits", e -> setLimits()));

        inputPanel.add(createButtonRow("Calculate", e -> calculateOptimization()));

        add(inputPanel, BorderLayout.WEST);

        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph(g);
            }
        };
        graphPanel.setBackground(Color.WHITE);
        add(graphPanel, BorderLayout.CENTER);

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setBackground(Color.BLACK);
        resultsArea.setForeground(Color.WHITE);
        resultsArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setPreferredSize(new Dimension(400, 0));
        add(scrollPane, BorderLayout.EAST);

        setVisible(true);
    }

    private void addNode() {
        String nodeName = nodeNameField.getText();
        int nodeWeight = Integer.parseInt(nodeWeightField.getText());
        nodes.put(nodeName, nodeWeight);
        nodePositions.put(nodeName, new Point((nodes.size() * 50) % graphPanel.getWidth(), (nodes.size() * 50) % graphPanel.getHeight()));
        resultsArea.append("Added Node: " + nodeName + " with weight " + nodeWeight + "\n");
        graphPanel.repaint();
    }

    private void addEdge() {
        String node1 = edgeNode1Field.getText();
        String node2 = edgeNode2Field.getText();
        int weight = Integer.parseInt(edgeWeightField.getText());

        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            resultsArea.append("Error: Both nodes must be added before creating an edge.\n");
            return;
        }

        edges.put(Arrays.asList(node1, node2), weight);
        resultsArea.append("Added Edge: " + node1 + " - " + node2 + " with weight " + weight + "\n");
        graphPanel.repaint();
    }

    private void setLimits() {
        try {
            int lowerLimit = Integer.parseInt(lowerLimitField.getText());
            int upperLimit = Integer.parseInt(upperLimitField.getText());
            GraphOptimization.setLimits(lowerLimit, upperLimit);
            resultsArea.append("Limits set: Lower Limit = " + lowerLimit + ", Upper Limit = " + upperLimit + "\n");
        } catch (NumberFormatException e) {
            resultsArea.append("Error: Limits must be numeric values.\n");
        }
    }

    private void calculateOptimization() {
        GraphOptimization.setNodes(nodes);
        GraphOptimization.setEdges(edges);

        String result = GraphOptimization.runAndDisplayResults(10, 10);
        resultsArea.append(result + "\n");

        int[] scoresGreedy = GraphOptimization.getScoresListForGreedy().stream().mapToInt(Integer::intValue).toArray();
        double[] timesGreedy = GraphOptimization.getTimesListForGreedy().stream().mapToDouble(Double::doubleValue).toArray();
        int[] scoresLocal = GraphOptimization.getScoresListForLocal().stream().mapToInt(Integer::intValue).toArray();
        double[] timesLocal = GraphOptimization.getTimesListForLocal().stream().mapToDouble(Double::doubleValue).toArray();
        int[] scoresAnnealing = GraphOptimization.getScoresListForAnnealing().stream().mapToInt(Integer::intValue).toArray();
        double[] timesAnnealing = GraphOptimization.getTimesListForAnnealing().stream().mapToDouble(Double::doubleValue).toArray();

        SimpleChart chart = new SimpleChart(scoresGreedy, timesGreedy, scoresLocal, timesLocal, scoresAnnealing, timesAnnealing);
        JFrame chartFrame = new JFrame("Results Chart");
        chartFrame.add(chart);
        chartFrame.setSize(600, 400);
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.setVisible(true);
    }

    private JPanel createLabelRow(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(Color.DARK_GRAY);
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        panel.add(label);
        panel.add(textField);
        return panel;
    }

    private JPanel createButtonRow(String buttonText, ActionListener listener) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(Color.DARK_GRAY);
        JButton button = new JButton(buttonText);
        button.addActionListener(listener);
        panel.add(button);
        return panel;
    }

    private void drawGraph(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.GRAY);
        for (Map.Entry<List<String>, Integer> edgeEntry : edges.entrySet()) {
            List<String> edgeNodes = edgeEntry.getKey();
            Point p1 = nodePositions.get(edgeNodes.get(0));
            Point p2 = nodePositions.get(edgeNodes.get(1));

            if (p1 != null && p2 != null) {
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                String weightLabel = String.valueOf(edgeEntry.getValue());
                g2.drawString(weightLabel, (p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
            }
        }

        g2.setColor(Color.BLUE);
        for (Map.Entry<String, Integer> nodeEntry : nodes.entrySet()) {
            String nodeName = nodeEntry.getKey();
            int nodeWeight = nodeEntry.getValue();
            Point p = nodePositions.get(nodeName);

            if (p != null) {
                g2.fillOval(p.x - 15, p.y - 15, 30, 30);
                g2.setColor(Color.BLACK);
                g2.drawOval(p.x - 15, p.y - 15, 30, 30);
                g2.drawString(nodeName + " (" + nodeWeight + ")", p.x - 10, p.y - 20);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GraphOptimizationGUI::new);
    }
}
