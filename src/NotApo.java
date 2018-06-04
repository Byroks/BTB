import Game.Controller.EAlignment;
import Game.Controller.EVirusType;
import Game.Controller.IPlayerController;
import Game.Logic.Base;
import Game.Logic.GameInformation;
import Game.Logic.Order;
import Game.Logic.Virus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class NotApo implements IPlayerController {
    private static final int TOO_FAR_AWAY_TO_ATTACK_ENEMY_TAKE_NEUTRAL = 5;
    private static final int DISTANCE_NEUTRAL_ENEMY = 3;
    private static final int COUNT_VIRUSES_FOR_ATTACK = 3;
    private static final int MAX_PRODUCTION_LEVEL = 2;
    private static final int MAX_NUMBER_DEFEND_DISTANCE = 3;
    private static final int MIN_DISTANCE_TO_UPGRADE = 13;
    private static final int TAKE_NEUTRAL_AFTER_X_ROUNDS = 50;
    private ArrayList<NotMyBase> myBases = new ArrayList();
    private ArrayList<NotMyBase> enemyBases = new ArrayList();
    private ArrayList<NotMyBase> neutralBases = new ArrayList();

    public NotApo() {
    }

    public String getAuthor() {
        return "Simon Gustavs (Kreativ Bot)";
    }

    public String getName() {
        return "Definitely not Eudemonia Solutions";
    }

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

    public class NotMyBase {
        private Base myBase;
        private int virusesAttack;
        private int virusesDefend;
        private int maxDurationAttack;
        private int maxDurationDefend;
        private final ArrayList<NotAttack> attacks;
        private final ArrayList<Virus> viruses;

        public NotMyBase(Base myBase, ArrayList<Virus> viruses) {
            this.myBase = myBase;
            this.viruses = viruses;
            this.attacks = new ArrayList();
            this.virusesAttack = 0;
            this.virusesDefend = 0;
            this.maxDurationAttack = 0;
            this.maxDurationDefend = 0;
            Iterator var3 = viruses.iterator();

            while(var3.hasNext()) {
                Virus virus = (Virus)var3.next();
                if (virus.getTarget().getId() == myBase.getId()) {
                    this.attacks.add(new NotAttack(virus.getDuration(), virus.getNumberOfViruses(), virus.getOwner()));
                    if (virus.getOwner() != myBase.getOwner()) {
                        this.virusesAttack += virus.getNumberOfViruses();
                        if (this.maxDurationAttack < virus.getDuration()) {
                            this.maxDurationAttack = virus.getDuration();
                        }
                    } else if (virus.getOwner() == myBase.getOwner()) {
                        this.virusesDefend += virus.getNumberOfViruses();
                        if (this.maxDurationDefend < virus.getDuration()) {
                            this.maxDurationDefend = virus.getDuration();
                        }
                    }
                }
            }

        }

        public Base getBase() {
            return this.myBase;
        }

        public int getVirusesDefend() {
            return this.virusesDefend;
        }

        public int getNumberOfViruses() {
            return this.myBase.getNumberOfViruses();
        }

        public EAlignment getOwner() {
            return this.myBase.getOwner();
        }

        public int getVirusesAttack() {
            return this.virusesAttack;
        }

        public final ArrayList<NotAttack> getAttacks() {
            return this.attacks;
        }

        public boolean willFall() {
            return !this.isGoodToSendVirusToDefend(0);
        }

        public boolean willFall(int duration) {
            return !this.isGoodToSendVirusToDefend(0, duration);
        }

        public boolean isGoodToSendVirusToDefend(int viruses) {
            int duration = 0;
            Iterator var3 = this.attacks.iterator();

            while(var3.hasNext()) {
                NotAttack attack = (NotAttack)var3.next();
                if (duration < attack.getDuration()) {
                    duration = attack.getDuration();
                }
            }

            return this.isGoodToSendVirusToDefend(viruses, duration);
        }

        public boolean isGoodToSendVirusToDefend(int viruses, int duration) {
            int myViruses = this.myBase.getNumberOfViruses() + viruses;
            if (this.myBase.getOwner() != EAlignment.Neutral) {
                myViruses += duration * this.myBase.getCurProductionLevel();
            }

            Iterator var4 = this.attacks.iterator();

            while(var4.hasNext()) {
                NotAttack attack = (NotAttack)var4.next();
                if (attack.getDuration() <= duration) {
                    if (attack.getOwner() != this.myBase.getOwner()) {
                        myViruses -= attack.getNumberOfViruses();
                    } else {
                        myViruses += attack.getNumberOfViruses();
                    }

                    if (myViruses <= 0) {
                        return false;
                    }
                }
            }

            return true;
        }

        public boolean meIsAttackingThePlanet() {
            Iterator var1 = this.viruses.iterator();

            Virus virus;
            do {
                if (!var1.hasNext()) {
                    return false;
                }

                virus = (Virus)var1.next();
            } while(virus.getOwner() != EAlignment.Friendly || virus.getTarget().getId() != this.myBase.getId() || this.myBase.getOwner() != EAlignment.Enemy);

            return true;
        }

        public int getMaxDurationUntilAttack() {
            return this.maxDurationAttack;
        }

        public int getDistanceToPlanet(ArrayList<NotMyBase> bases, GameInformation info) {
            int distance = 1000000000;
            Iterator var4 = bases.iterator();

            while(var4.hasNext()) {
                NotMyBase enemyBase = (NotMyBase)var4.next();
                int distanceBetweenBases = info.getDistanceBetweenBases(enemyBase.getBase(), this.myBase);
                if (distance > distanceBetweenBases) {
                    distance = distanceBetweenBases;
                }
            }

            return distance;
        }
    }


    public Order think(ArrayList<Base> bases, ArrayList<Virus> viruses, GameInformation info) {
        NotMyBase origin = null;
        this.setAndFillBases(bases, viruses);
        Order orderGoToOther = this.orderGoToOther(bases, viruses, info);
        if (orderGoToOther != null) {
            return orderGoToOther;
        } else {
            Order orderUpgradeIfPlanetZero = this.orderUpgradeIfPlanetZero(info);
            if (orderUpgradeIfPlanetZero != null) {
                return orderUpgradeIfPlanetZero;
            } else {
                Order orderShieldWhenAttackedNextRound = this.orderShieldWhenAttackNextRound(viruses, info);
                if (orderShieldWhenAttackedNextRound != null) {
                    return orderShieldWhenAttackedNextRound;
                } else {
                    Order checkWillFallPlanet = this.checkWillFallPlanet(bases, info);
                    if (checkWillFallPlanet != null) {
                        return checkWillFallPlanet;
                    } else {
                        Order upgradeIfFarAway = this.checkUpgradeIfFarAway(bases, info);
                        if (upgradeIfFarAway != null) {
                            return upgradeIfFarAway;
                        } else {
                            origin = this.getOriginBase(bases, info);
                            if (origin == null) {
                                return this.returnOrderForNoOrigin(info);
                            } else {
                                Base enemy = this.getEnemy(info, origin);
                                if (enemy == null) {
                                    return Order.Idle();
                                } else {
                                    int amount = this.getAmountToAttack(info, origin, enemy);
                                    return new Order(origin.getBase(), enemy, amount);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Order orderGoToOther(ArrayList<Base> bases, ArrayList<Virus> viruses, GameInformation info) {
        int ticksPassed = info.getMaximalGameTicks() - info.getRemaningGameticks();
        if (ticksPassed > 80 && this.myBases.size() == this.enemyBases.size() && this.myBases.size() < 3) {
            NotMyBase origin = this.getOriginBase(bases, info);
            if (origin == null) {
                return null;
            }

            Base enemy = null;
            int distance = 100000;
            boolean bNeutral = false;
            Iterator var9 = this.neutralBases.iterator();

            while(var9.hasNext()) {
                NotMyBase neut = (NotMyBase)var9.next();
                int newDist = info.getDistanceBetweenBases(origin.getBase(), neut.getBase());
                if (newDist < distance && !neut.willFall() && !neut.meIsAttackingThePlanet() && this.noNeutralFindYetOrNeutralDistanceCheck(bNeutral, distance - newDist) && this.isOnlyFiveNeutralOrMoreThanXTicks(ticksPassed, neut)) {
                    distance = newDist;
                    enemy = neut.getBase();
                    bNeutral = true;
                }
            }

            if (enemy != null) {
                int amount = this.getAmountToAttack(info, origin, enemy);
                return new Order(origin.getBase(), enemy, amount);
            }
        }

        return null;
    }

    private Order orderShieldWhenAttackNextRound(ArrayList<Virus> viruses, GameInformation info) {
        Iterator var3 = viruses.iterator();

        while(true) {
            Virus virus;
            do {
                do {
                    if (!var3.hasNext()) {
                        return null;
                    }

                    virus = (Virus)var3.next();
                } while(virus.get_specialAttack() != EVirusType.Rocket);
            } while(virus.getDuration() > 2);

            Iterator var5 = this.myBases.iterator();

            while(var5.hasNext()) {
                NotMyBase base = (NotMyBase)var5.next();
                if (base.getBase().getId() == virus.getTarget().getId() && (float)base.getNumberOfViruses() > (float)info.getRocketCosts() * 1.5F && !base.getBase().getBoost()) {
                    return Order.Shield(base.getBase());
                }
            }
        }
    }

    private int getAmountToAttack(GameInformation info, NotMyBase origin, Base enemy) {
        int amount = origin.getNumberOfViruses() - origin.getVirusesAttack();
        int distance = info.getDistanceBetweenBases(origin.getBase(), enemy);
        int sendAmoutToenemyBaseViruses = enemy.getNumberOfViruses() + distance * enemy.getCurProductionLevel();
        if (amount - sendAmoutToenemyBaseViruses > 3) {
            amount = sendAmoutToenemyBaseViruses + 1;
        }

        if (enemy.getOwner() == EAlignment.Neutral) {
            amount = enemy.getNumberOfViruses() + 1;
        }

        if (info.getMaximalGameTicks() - info.getRemaningGameticks() < 5) {
            amount = origin.getNumberOfViruses() - origin.getVirusesAttack();
        }

        return amount;
    }

    private Order checkUpgradeIfFarAway(ArrayList<Base> bases, GameInformation info) {
        for(int i = 0; i < this.myBases.size(); ++i) {
            int minDistance = 1000;
            NotMyBase myBase = (NotMyBase)this.myBases.get(i);
            Iterator var6 = this.enemyBases.iterator();

            while(var6.hasNext()) {
                NotMyBase enemyBase = (NotMyBase)var6.next();
                int distance = info.getDistanceBetweenBases(myBase.getBase(), enemyBase.getBase());
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }

            if (minDistance > 13 && myBase.getNumberOfViruses() > info.getUpgradeCost(myBase.getBase().getCurProductionLevel()) + 3 && myBase.getVirusesAttack() <= 0 && myBase.getBase().getCurProductionLevel() < 2) {
                return Order.Upgrade(myBase.getBase());
            }
        }

        return null;
    }

    private Order checkWillFallPlanet(ArrayList<Base> bases, GameInformation info) {
        label41:
        for(int i = 0; i < this.myBases.size(); ++i) {
            NotMyBase myBase = (NotMyBase)this.myBases.get(i);
            int viruses = 0;
            int sendViruses = 0;
            NotMyBase saveOrigin = null;
            if (myBase.willFall()) {
                Iterator var8 = this.myBases.iterator();

                while(true) {
                    NotMyBase checkBases;
                    int curSendViruses;
                    do {
                        int checkDefendPlanet;
                        do {
                            if (!var8.hasNext()) {
                                if (saveOrigin != null && myBase.isGoodToSendVirusToDefend(viruses)) {
                                    return new Order(saveOrigin.getBase(), myBase.getBase(), sendViruses);
                                }
                                continue label41;
                            }

                            checkBases = (NotMyBase)var8.next();
                            checkDefendPlanet = this.checkDefendPlanet(myBase, checkBases, info);
                        } while(checkDefendPlanet <= 0);

                        curSendViruses = checkBases.getNumberOfViruses() - checkBases.getVirusesAttack();
                        viruses += curSendViruses;
                    } while(saveOrigin != null && sendViruses >= curSendViruses);

                    saveOrigin = checkBases;
                    sendViruses = curSendViruses;
                }
            }
        }

        return null;
    }

    private int checkDefendPlanet(NotMyBase myBase, NotMyBase checkBases, GameInformation info) {
        if (checkBases.getBase().getId() == myBase.getBase().getId()) {
            return -1;
        } else if (checkBases.getNumberOfViruses() - checkBases.getVirusesAttack() < 1) {
            return -1;
        } else {
            int distance = info.getDistanceBetweenBases(myBase.getBase(), checkBases.getBase());
            if (distance > 3) {
                return -1;
            } else {
                return myBase.getMaxDurationUntilAttack() >= distance ? distance : -1;
            }
        }
    }

    private NotMyBase getOriginBase(ArrayList<Base> bases, GameInformation info) {
        NotMyBase origin = null;
        Map<NotMyBase, Base> bestValueBase = new HashMap();

        int bestValue = 0;
        for(bestValue = 0; bestValue < this.myBases.size(); bestValue++) {
            NotMyBase myBase = this.myBases.get(bestValue);
            if (myBase.getNumberOfViruses() >= 3 && (myBase.getVirusesAttack() <= 0 && myBase.getNumberOfViruses() <= 10 || myBase.getNumberOfViruses() - myBase.getVirusesAttack() > 3)) {
                bestValue = 100000;
                Iterator var8 = bases.iterator();

                while(var8.hasNext()) {
                    Base enemy = (Base)var8.next();
                    if (enemy.getOwner() != EAlignment.Friendly) {
                        int valueForBase = this.getValueForBase(myBase, enemy, info);
                        if (valueForBase < bestValue && valueForBase>=0) {
                            bestValue = valueForBase;
                            bestValueBase.put(myBase, enemy);
                        }
                    }
                }
            }
        }

        bestValue = 10000000;
        Iterator var11 = bestValueBase.entrySet().iterator();

        while(var11.hasNext()) {
            Entry<NotMyBase, Base> base = (Entry)var11.next();
            int value = this.getValueForBase((NotMyBase)base.getKey(), (Base)base.getValue(), info);
            if (value < bestValue) {
                bestValue = value;
                origin = (NotMyBase)base.getKey();
            }
        }

        return origin;
    }

    private int getValueForBase(NotMyBase myBase, Base enemy, GameInformation info) {
        if (myBase.getNumberOfViruses() < 3) {
            return 1000000;
        } else {
            int value = info.getDistanceBetweenBases(myBase.getBase(), enemy) * 2;
            value += (50 - myBase.getBase().getNumberOfViruses()) * 4;
            value -= myBase.getNumberOfViruses() - enemy.getNumberOfViruses();
            if (enemy.getOwner() == EAlignment.Neutral) {
                value += 5;
            }

            if (enemy.getCurProductionLevel() > 1) {
                value -= enemy.getCurProductionLevel() * 5;
            }

            if (myBase.getBase().getCurProductionLevel() > 1) {
                value -= myBase.getBase().getCurProductionLevel() * 3;
            }

            return value;
        }
    }

    private void setAndFillBases(ArrayList<Base> bases, ArrayList<Virus> viruses) {
        this.myBases.clear();
        this.enemyBases.clear();
        this.neutralBases.clear();
        Iterator var3 = bases.iterator();

        while(var3.hasNext()) {
            Base base = (Base)var3.next();
            if (base.getOwner() == EAlignment.Friendly) {
                this.myBases.add(new NotMyBase(base, viruses));
            } else if (base.getOwner() == EAlignment.Enemy) {
                this.enemyBases.add(new NotMyBase(base, viruses));
            } else {
                this.neutralBases.add(new NotMyBase(base, viruses));
            }
        }

    }

    private Base getEnemy(GameInformation info, NotMyBase origin) {
        Base enemy = null;
        int distance = 100000;
        int value = 10000;
        Iterator var6 = this.enemyBases.iterator();

        NotMyBase willFallBase;
        int newDist;
        while(var6.hasNext()) {
            willFallBase = (NotMyBase)var6.next();
            newDist = info.getDistanceBetweenBases(origin.getBase(), willFallBase.getBase());
            int curValue = this.getValueForEnemy(origin, willFallBase.getBase(), info);
            if (curValue < value) {
                enemy = willFallBase.getBase();
                value = curValue;
                distance = newDist;
            }
        }

        var6 = this.myBases.iterator();

        while(var6.hasNext()) {
            willFallBase = (NotMyBase)var6.next();
            if (willFallBase.getBase().getId() != origin.getBase().getId() && willFallBase.willFall()) {
                newDist = info.getDistanceBetweenBases(origin.getBase(), willFallBase.getBase());
                if (newDist < distance) {
                    distance = newDist;
                    enemy = willFallBase.getBase();
                }
            }
        }

        int ticksPassed = info.getMaximalGameTicks() - info.getRemaningGameticks();
        boolean bNeutral = false;
        Iterator var13 = this.neutralBases.iterator();

        while(true) {
            NotMyBase neut;
            do {
                do {
                    do {
                        if (!var13.hasNext()) {
                            return enemy;
                        }

                        neut = (NotMyBase)var13.next();
                        newDist = info.getDistanceBetweenBases(origin.getBase(), neut.getBase());
                    } while(newDist >= distance);
                } while(neut.willFall());
            } while(origin.getNumberOfViruses() - origin.getVirusesAttack() <= neut.getNumberOfViruses() && distance + 5 >= newDist);

            if (!neut.meIsAttackingThePlanet() && this.noNeutralFindYetOrNeutralDistanceCheck(bNeutral, distance - newDist) && this.isOnlyFiveNeutralOrMoreThanXTicks(ticksPassed, neut)) {
                distance = newDist;
                enemy = neut.getBase();
                bNeutral = true;
            }
        }
    }

    private int getValueForEnemy(NotMyBase myBase, Base enemy, GameInformation info) {
        int value = info.getDistanceBetweenBases(myBase.getBase(), enemy);
        if (enemy.getCurProductionLevel() > 1) {
            value += enemy.getCurProductionLevel() * 1;
        }

        return value;
    }

    private boolean noNeutralFindYetOrNeutralDistanceCheck(boolean bNeutral, int dist) {
        return bNeutral || dist > 3;
    }

    private boolean isOnlyFiveNeutralOrMoreThanXTicks(int ticksPassed, NotMyBase neut) {
        return ticksPassed > 50 || neut.getNumberOfViruses() <= 5;
    }

    private Order orderUpgradeIfPlanetZero(GameInformation info) {
        Iterator var2 = this.myBases.iterator();

        Base base;
        int curProductionLevel;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            NotMyBase myBase = (NotMyBase)var2.next();
            base = myBase.getBase();
            curProductionLevel = base.getCurProductionLevel();
        } while(curProductionLevel != 0 || base.getNumberOfViruses() < info.getUpgradeCost(curProductionLevel));

        return Order.Upgrade(base);
    }

    private Order returnOrderForNoOrigin(GameInformation info) {
        Iterator var2 = this.myBases.iterator();

        NotMyBase myBase;
        Base base;
        int upgradeCost;
        do {
            if (!var2.hasNext()) {
                return Order.Idle();
            }

            myBase = (NotMyBase)var2.next();
            base = myBase.getBase();
            upgradeCost = info.getUpgradeCost(base.getCurProductionLevel());
        } while(base.getCurProductionLevel() >= 2 || myBase.getVirusesAttack() >= base.getNumberOfViruses() - upgradeCost || base.getNumberOfViruses() <= upgradeCost);

        return Order.Upgrade(base);
    }
}
