package Kreativbot;

import Game.Controller.EAlignment;

public class NotAttack {
    private int duration;
    private EAlignment owner;
    private int numberOfViruses;

    public NotAttack(int duration, int numberOfViruses, EAlignment owner) {
        this.duration = duration;
        this.numberOfViruses = numberOfViruses;
        this.owner = owner;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getNumberOfViruses() {
        return this.numberOfViruses;
    }

    public EAlignment getOwner() {
        return this.owner;
    }
}
