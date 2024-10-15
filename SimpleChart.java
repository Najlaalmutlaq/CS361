import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

class SimpleChart extends JPanel {
    private int[] scoresGreedy, scoresLocal, scoresAnnealing;
    private double[] timesGreedy, timesLocal, timesAnnealing;

    public SimpleChart(int[] scoresGreedy, double[] timesGreedy, int[] scoresLocal, double[] timesLocal, int[] scoresAnnealing, double[] timesAnnealing) {
        this.scoresGreedy = scoresGreedy;
        this.timesGreedy = timesGreedy;
        this.scoresLocal = scoresLocal;
        this.timesLocal = timesLocal;
        this.scoresAnnealing = scoresAnnealing;
        this.timesAnnealing = timesAnnealing;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        g2.drawLine(50, height - 50, width - 50, height - 50);
        g2.drawLine(50, height - 50, 50, 50);

        plotData(g2, scoresGreedy, timesGreedy, Color.BLUE);
        plotData(g2, scoresLocal, timesLocal, Color.RED);
        plotData(g2, scoresAnnealing, timesAnnealing, Color.GREEN);

        g2.setColor(Color.BLACK);
        g2.drawString("Run", width / 2, height - 20);
        g2.drawString("Score/Time", 20, height / 2);
    }

    private void plotData(Graphics2D g2, int[] scores, double[] times, Color color) {
        int width = getWidth();
        int height = getHeight();

        g2.setColor(color);
        int maxScore = Arrays.stream(scores).max().orElse(1);
        double maxTime = Arrays.stream(times).max().orElse(1.0);

        for (int i = 0; i < scores.length; i++) {
            int x = 50 + i * (width - 100) / (scores.length - 1);
            int yScore = height - 50 - (int) ((scores[i] / (double) maxScore) * (height - 100));
            g2.fillOval(x - 3, yScore - 3, 6, 6);
            if (i > 0) {
                int prevX = 50 + (i - 1) * (width - 100) / (scores.length - 1);
                int prevYScore = height - 50 - (int) ((scores[i - 1] / (double) maxScore) * (height - 100));
                g2.drawLine(prevX, prevYScore, x, yScore);
            }

            int yTime = height - 50 - (int) ((times[i] / maxTime) * (height - 100));
            g2.fillOval(x - 3, yTime - 3, 6, 6);
            if (i > 0) {
                int prevX = 50 + (i - 1) * (width - 100) / (times.length - 1);
                int prevYTime = height - 50 - (int) ((times[i - 1] / maxTime) * (height - 100));
                g2.drawLine(prevX, prevYTime, x, yTime);
            }
        }
    }
}

