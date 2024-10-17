import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GraphOptimizationGUI extends JFrame {
    private JTextField nodeNameField, nodeWeightField, edgeNode1Field, edgeNode2Field, edgeWeightField;
    private JTextField lowerLimitField, upperLimitField;
    private JTextArea resultsArea;
    private JPanel graphPanel;
    private Map<String, Integer> nodes = new HashMap<>();
    private Map<List<String>, Integer> edges = new HashMap<>();
    private Map<String, Point> nodePositions = new HashMap<>();
    private static final int NODE_RADIUS = 20;

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

        // توزيع العقد في مركز الرسم مع تباعد بينها
        int x, y;
        do {
            x = (int) (Math.random() * (graphPanel.getWidth() - 2 * NODE_RADIUS)) + NODE_RADIUS;
            y = (int) (Math.random() * (graphPanel.getHeight() - 2 * NODE_RADIUS)) + NODE_RADIUS;
        } while (isOverlapping(new Point(x, y)));

        nodePositions.put(nodeName, new Point(x, y));
        resultsArea.append("Added Node: " + nodeName + " with weight " + nodeWeight + "\n");
        graphPanel.repaint();
    }

    private boolean isOverlapping(Point position) {
        for (Point point : nodePositions.values()) {
            if (point.distance(position) < 2 * NODE_RADIUS) {
                return true;
            }
        }
        return false;
    }

    private void addEdge() {
        String node1 = edgeNode1Field.getText();
        String node2 = edgeNode2Field.getText();
        int weight = Integer.parseInt(edgeWeightField.getText());

        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            resultsArea.append("Error: Both nodes must be added before creating an edge.\n");
            return;
        }

        // Check for edge intersection
        if (checkEdgeIntersection(node1, node2)) {
            resultsArea.append("Error: Edge intersects with existing edges.\n");
            return;
        }

        edges.put(Arrays.asList(node1, node2), weight);
        resultsArea.append("Added Edge: " + node1 + " - " + node2 + " with weight " + weight + "\n");
        graphPanel.repaint();
    }

    private boolean checkEdgeIntersection(String node1, String node2) {
        Point p1 = nodePositions.get(node1);
        Point p2 = nodePositions.get(node2);

        for (Map.Entry<List<String>, Integer> edgeEntry : edges.entrySet()) {
            List<String> existingEdgeNodes = edgeEntry.getKey();
            Point p3 = nodePositions.get(existingEdgeNodes.get(0));
            Point p4 = nodePositions.get(existingEdgeNodes.get(1));

            if (linesIntersect(p1, p2, p3, p4)) {
                return true;
            }
        }
        return false;
    }

    private boolean linesIntersect(Point p1, Point p2, Point p3, Point p4) {
        double det = (p1.x - p2.x) * (p3.y - p4.y) - (p3.x - p4.x) * (p1.y - p2.y);
        if (det == 0) return false; // Lines are parallel

        double lambda = ((p3.y - p4.y) * (p3.x - p1.x) + (p4.x - p3.x) * (p3.y - p1.y)) / det;
        double gamma = ((p1.y - p2.y) * (p3.x - p1.x) + (p2.x - p1.x) * (p3.y - p1.y)) / det;

        return (0 < lambda && lambda < 1) && (0 < gamma && gamma < 1);
    }

    private void setLimits() {
        try {
            int lowerLimit = Integer.parseInt(lowerLimitField.getText());
            int upperLimit = Integer.parseInt(upperLimitField.getText());
            GraphOptimization.setLimits(lowerLimit, upperLimit);
            // تحتاج إلى تنفيذ setLimits هنا
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

        // رسم الحواف
        g2.setColor(new Color(150, 150, 150)); // لون رمادي أفتح
        for (Map.Entry<List<String>, Integer> edgeEntry : edges.entrySet()) {
            List<String> edgeNodes = edgeEntry.getKey();
            Point p1 = nodePositions.get(edgeNodes.get(0));
            Point p2 = nodePositions.get(edgeNodes.get(1));

            if (p1 != null && p2 != null) {
                g2.setStroke(new BasicStroke(2)); // سمك الخط
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                String weightLabel = String.valueOf(edgeEntry.getValue());
                g2.drawString(weightLabel, (p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
            }
        }

        // رسم العقد
        g2.setColor(Color.BLUE);
        for (Map.Entry<String, Integer> nodeEntry : nodes.entrySet()) {
            String nodeName = nodeEntry.getKey();
            int nodeWeight = nodeEntry.getValue();
            Point p = nodePositions.get(nodeName);

            if (p != null) {
                g2.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS); // حجم الدائرة
                g2.setColor(Color.BLACK);
                g2.drawOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
                g2.drawString(nodeName + " (" + nodeWeight + ")", p.x - 10, p.y - 30); // موضع النص أعلى العقدة
                g2.setColor(Color.BLUE); // العودة إلى اللون الأزرق للعقد
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GraphOptimizationGUI::new);
    }
}

