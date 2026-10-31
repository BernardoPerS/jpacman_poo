package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a circuit that connects switches and gates.
 * A switch toggles the circuit command, which is propagated
 * to all gates belonging to the circuit.
 *
 * @author Bernardo Pereira Silva
 */
public class Circuit {

    /**
     * Circuit identifier.
     */
    private final int group;

    /**
     * Current command state of the circuit.
     */
    private int state = Gate.CLOSED;

    /**
     * Gates controlled by this circuit.
     */
    private final List<Gate> gates = new ArrayList<>();

    /**
     * Switches associated with this circuit.
     */
    private final List<Switch> switches = new ArrayList<>();

    /**
     * Creates a new circuit.
     *
     * @param group The circuit identifier.
     */
    public Circuit(int group) {
        this.group = group;
    }

    /**
     * Returns the circuit identifier.
     *
     * @return The group identifier.
     */
    public int getGroup() {
        return group;
    }

    /**
     * Returns the current command state.
     *
     * @return OPEN, CLOSED or CLOSING.
     */
    public int getState() {
        return state;
    }

    /**
     * Registers a gate in this circuit.
     *
     * @param gate The gate to register.
     */
    public void addGate(Gate gate) {
        if (gate != null && gate.getGroup() == group && !gates.contains(gate)) {
            gates.add(gate);
            gate.setState(state);
        }
    }

    /**
     * Registers a switch in this circuit.
     *
     * @param sw The switch to register.
     */
    public void addSwitch(Switch sw) {
        if (sw != null && sw.getGroup() == group && !switches.contains(sw)) {
            switches.add(sw);
            sw.setCircuit(this);
        }
    }

    /**
     * Defines a new state and notifies all its gates.
     *
     * @param newState The new state to be set.
     */
    public void setState(int newState) {
        this.state = newState;
        for (Gate gate : gates) {
            gate.setState(newState);
        }
    }

    /**
     * Activates the circuit command (sets to OPEN).
     */
    public void toggle() {
        setState(state == Gate.OPEN ? Gate.CLOSED : Gate.OPEN);
    }

    /**
     * Returns an unmodifiable list of gates.
     *
     * @return Gates belonging to this circuit.
     */
    public List<Gate> getGates() {
        return Collections.unmodifiableList(gates);
    }

    /**
     * Returns an unmodifiable list of switches.
     *
     * @return Switches belonging to this circuit.
     */
    public List<Switch> getSwitches() {
        return Collections.unmodifiableList(switches);
    }
}