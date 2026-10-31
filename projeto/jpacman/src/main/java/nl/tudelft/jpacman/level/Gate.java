
package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A gate that can be opened and closed by a circuit.
 *
 * @author Bernardo Pereira Silva
 */
public class Gate extends Square {

    public static final int CLOSED = 0;
    public static final int OPEN = 1;
    public static final int CLOSING = 2;

    private final int group;
    private int state = CLOSED;

    private final Sprite openSprite;
    private final Sprite closeSprite;

    public Gate(int group, Sprite closeSprite, Sprite openSprite) {
        this.group = group;
        this.closeSprite = closeSprite;
        this.openSprite = openSprite;
    }

    public int getGroup() {
        return group;
    }

    public int getState() {
        return state;
    }

    /**
     * Receives a command from the circuit.
     *
     * @param newState OPEN or CLOSED.
     */
    public void setState(int newState) {
        if (newState == OPEN) {
            state = OPEN;
        } else if (newState == CLOSED) {
            if (getOccupants().isEmpty()) {
                state = CLOSED;
            } else {
                state = CLOSING;
            }
        } else {
            throw new IllegalArgumentException("Invalid gate command.");
        }
    }

    /**
     * Called when a unit leaves this square.
     * A pending gate closes when its last occupant leaves.
     */
    @Override
    protected void remove(Unit occupant) {
        super.remove(occupant);

        if (state == CLOSING && getOccupants().isEmpty()) {
            state = CLOSED;
        }
    }

    @Override
    public boolean isAccessibleTo(Unit unit) {
        return state == OPEN;
    }

    @Override
    public Sprite getSprite() {
        return state == OPEN ? openSprite : closeSprite;
    }
}
