package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

public class Pulse extends Unit {

    private final Sprite image;
    private final int group;
    private Circuit circuit;

    public Pulse(int group, Sprite sprite) {
        this.group = group;
        this.image = sprite;
    }

    public int getGroup() {
        return group;
    }

    void setCircuit(Circuit circuit) {
        this.circuit = circuit;
    }

    public void activate() {
        circuit.activatePulse();
    }

    @Override
    public Sprite getSprite() {
        return image;
    }
}
