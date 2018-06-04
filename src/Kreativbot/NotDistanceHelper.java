package Kreativbot;

import Game.Logic.Base;

public class NotDistanceHelper {
    private final NotMyBase startBase;
    private final Base enemy;
    private final int distance;

    public NotDistanceHelper(NotMyBase startBase, Base enemy, int distance) {
        this.startBase = startBase;
        this.enemy = enemy;
        this.distance = distance;
    }

    public NotMyBase getStartBase() {
        return this.startBase;
    }

    public Base getEnemy() {
        return this.enemy;
    }

    public int getDistance() {
        return this.distance;
    }
}
