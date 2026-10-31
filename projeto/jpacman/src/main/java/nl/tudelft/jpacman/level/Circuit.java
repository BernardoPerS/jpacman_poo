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

    private long pulseTime = 0;

    /**
     * Gates controlled by this circuit.
     */
    private final List<Gate> gates = new ArrayList<>();

    /**
     * Switches associated with this circuit.
     */
    private final List<Switch> switches = new ArrayList<>();

    private final List<Pulse> pulses = new ArrayList<>();

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

    public boolean isPulseActive() {
        return pulseTime > 0;
    }

    public int getAppliedCommand() {
        if (isPulseActive()) {
            return state == Gate.OPEN ? Gate.CLOSED : Gate.OPEN;
        }
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
            gate.setState(getAppliedCommand());
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

    public void addPulse(Pulse pulse) {
        if (pulse != null && pulse.getGroup() == group && !pulses.contains(pulse)) {
            pulses.add(pulse);
            pulse.setCircuit(this);
        }
    }

    private void updateGates() {
        int cmd = getAppliedCommand();
        for (Gate gate : gates) {
            gate.setState(cmd);
        }
    }

    /**
     * Defines a new state and notifies all its gates.
     *
     * @param newState The new state to be set.
     */
    public void setState(int newState) {
        this.state = newState;
        updateGates();
    }

    /**
     * Activates the circuit command (sets to OPEN).
     */
    public void toggle() {
        setState(state == Gate.OPEN ? Gate.CLOSED : Gate.OPEN);
    }

    public void activatePulse() {
        this.pulseTime = 5000;
        updateGates();
    }

    public void updatePulse(long delta) {
        if (pulseTime > 0) {
            pulseTime -= delta;
            if (pulseTime <= 0) {
                pulseTime = 0;
                updateGates();
            }
        }
    }

    public boolean isValid() {
        if (switches.isEmpty() && gates.isEmpty() && pulses.isEmpty()) {
            return true;
        }
        return !switches.isEmpty() && !gates.isEmpty();
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

    public List<Pulse> getPulses() {
        return Collections.unmodifiableList(pulses);
    }
}
