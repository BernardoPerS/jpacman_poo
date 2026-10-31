package nl.tudelft.jpacman.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.game.Game;
import nl.tudelft.jpacman.level.Circuit;
import nl.tudelft.jpacman.level.Gate;
import nl.tudelft.jpacman.level.Pulse;
import nl.tudelft.jpacman.level.Switch;

/**
 * Panel displaying a game.
 *
 * @author Jeroen Roosen 
 *
 */
class BoardPanel extends JPanel {

    /**
     * Default serialisation ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The background colour of the board.
     */
    private static final Color BACKGROUND_COLOR = Color.BLACK;

    /**
     * The size (in pixels) of a square on the board. The initial size of this
     * panel will scale to fit a board with square of this size.
     */
    private static final int SQUARE_SIZE = 16;

    /**
     * The game to display.
     */
    private final Game game;

    /**
     * Creates a new board panel that will display the provided game.
     *
     * @param game
     *            The game to display.
     */
    BoardPanel(Game game) {
        super();
        assert game != null;
        this.game = game;

        Board board = game.getLevel().getBoard();

        int w = board.getWidth() * SQUARE_SIZE;
        int h = board.getHeight() * SQUARE_SIZE;

        Dimension size = new Dimension(w, h);
        setMinimumSize(size);
        setPreferredSize(size);
    }

    @Override
    public void paint(Graphics g) {
        assert g != null;
        render(game.getLevel().getBoard(), g, getSize());
    }

    /**
     * Renders the board on the given graphics context to the given dimensions.
     *
     * @param board
     *            The board to render.
     * @param graphics
     *            The graphics context to draw on.
     * @param window
     *            The dimensions to scale the rendered board to.
     */
    private void render(Board board, Graphics graphics, Dimension window) {
        int cellW = window.width / board.getWidth();
        int cellH = window.height / board.getHeight();

        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, window.width, window.height);

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                int cellX = x * cellW;
                int cellY = y * cellH;
                Square square = board.squareAt(x, y);
                render(square, graphics, cellX, cellY, cellW, cellH);
            }
        }

        graphics.setColor(Color.WHITE);
        StringBuilder status = new StringBuilder("Chave=Switch | Sino=Pulso | !=Pendente");
        for (Circuit c : game.getLevel().getCircuits()) {
            if (c.isPulseActive()) {
                status.append(" | Pulso C").append(c.getGroup()).append(": ATIVO");
            }
        }
        graphics.drawString(status.toString(), 4, 12);
    }

    /**
     * Renders a single square on the given graphics context on the specified
     * rectangle.
     *
     * @param square
     *            The square to render.
     * @param graphics
     *            The graphics context to draw on.
     * @param x
     *            The x position to start drawing.
     * @param y
     *            The y position to start drawing.
     * @param width
     *            The width of this square (in pixels.)
     * @param height
     *            The height of this square (in pixels.)
     */
    private void render(Square square, Graphics graphics, int x, int y, int width, int height) {
        square.getSprite().draw(graphics, x, y, width, height);
        for (Unit unit : square.getOccupants()) {
            unit.getSprite().draw(graphics, x, y, width, height);
            if (unit instanceof Switch) {
                graphics.setColor(Color.CYAN);
                graphics.drawString(String.valueOf(((Switch) unit).getGroup()), x + 2, y + 12);
            } else if (unit instanceof Pulse) {
                graphics.setColor(Color.GREEN);
                graphics.drawString(String.valueOf(((Pulse) unit).getGroup()), x + 2, y + 12);
            }
        }
        if (square instanceof Gate) {
            Gate gate = (Gate) square;
            if (gate.getState() == Gate.CLOSING) {
                graphics.setColor(Color.RED);
                graphics.drawRect(x + 1, y + 1, width - 3, height - 3);
                graphics.drawString(gate.getGroup() + "!", x + 2, y + 12);
            } else {
                graphics.setColor(Color.YELLOW);
                graphics.drawString(String.valueOf(gate.getGroup()), x + 2, y + 12);
            }
        }
    }
}