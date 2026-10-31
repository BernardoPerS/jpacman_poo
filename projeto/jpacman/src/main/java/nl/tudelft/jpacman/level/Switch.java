
package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A switch that toggles its associated circuit.
 *
 * @author Bernardo Pereira Silva
 */
public class Switch extends Unit {

    private final Sprite image;
    private final int group;
    private Circuit circuit;

    /**
     * Creates a new switch.
     *
     * @param group The circuit identifier.
     * @param sprite The switch sprite.
     */
    public Switch(int group, Sprite sprite) {
        this.group = group;
        this.image = sprite;
    }

    public int getGroup() {
        return group;
    }

    /**
     * Associates this switch with a circuit.
     *
     * @param circuit The circuit this switch belongs to.
     */
    void setCircuit(Circuit circuit) {
        if (circuit == null || circuit.getGroup() != group) {
            throw new IllegalArgumentException("Invalid circuit for switch.");
        }
        this.circuit = circuit;
    }

    /**
     * Returns the current command state of the circuit.
     */
    public int getState() {
        if (circuit == null) {
            return Gate.CLOSED;
        }
        return circuit.getState();
    }

    /**
     * Activates the associated circuit.
     */
    public void changeState() {
        if (circuit == null) {
            throw new IllegalStateException(
                "Switch has not been associated with a circuit."
            );
        }
        circuit.toggle();
    }

    @Override
    public Sprite getSprite() {
        return image;
    }
}
