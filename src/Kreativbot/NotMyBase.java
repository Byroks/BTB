package Kreativbot;

import Game.Controller.EAlignment;
import Game.Logic.Base;
import Game.Logic.GameInformation;
import Game.Logic.Virus;
import java.util.ArrayList;
import java.util.Iterator;

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
