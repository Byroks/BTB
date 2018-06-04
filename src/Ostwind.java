import Game.Controller.EAlignment;
import Game.Controller.IPlayerController;
import Game.Logic.*;
import java.util.ArrayList;

public class Ostwind implements IPlayerController {
    private Base BaseDefend = null;

	@Override
	public String getAuthor() {
		return "Simon Gustavs";
	}

	@Override
	public String getName() {
		return "Der Ostwind";
	}
	
	private Boolean checkDist(Base origin, Base target, int dist, GameInformation arg2) {
		return dist > arg2.getDistanceBetweenBases(origin, target);
	}
	
	private Base searchTarget(Base origin, ArrayList<Base> arg0, GameInformation arg2) {
		Base targetD = null;
		Base target = null;
		int dist = 250;


        for (Base anArg0 : arg0) {
            targetD = anArg0;
            if (targetD.getOwner() == EAlignment.Enemy && checkDist(origin, targetD, dist, arg2)) {
                target = targetD;
                dist = arg2.getDistanceBetweenBases(origin, target);
            }
        }

		if(target ==  null){
		    return targetD;
        }
		return target;
	}

    private Base searchNeutral(Base origin, ArrayList<Base> arg0, float dist) {
        Base targetD;
        Base target = null;

        for (Base anArg0 : arg0) {
            targetD = anArg0;
            if (targetD.getOwner() == EAlignment.Neutral && origin.getPosition().distance(targetD.getPosition()) < dist) {
                target = targetD;
            }
        }

        return target;
    }

    private boolean checkUp(Base origin, ArrayList<Base> arg0, GameInformation arg2){
        for (Base anArg0 : arg0) {
            if (anArg0.getOwner() != EAlignment.Friendly && checkDist(origin, anArg0, 8, arg2)) {
                return false;
            }
        }

        return true;
    }

    private int isTarget(Base origin, ArrayList<Virus> arg1){
	    int sum = 0;
	    if(arg1 != null) {
            for (Virus anArg1 : arg1) {
                if (anArg1.getTarget() == origin) {
                    sum += anArg1.getNumberOfViruses();
                }
            }
        }
        return sum;
    }

    private int getDmg(Base origin, ArrayList<Virus> arg1){
        int sum = 0;
        if(arg1 != null) {
            for (Virus anArg1 : arg1) {
                if (anArg1.getTarget().getId() == origin.getId()) {
                    if (anArg1.getOwner() == EAlignment.Enemy) {
                        sum += anArg1.getNumberOfViruses();
                    } else {
                        sum -= anArg1.getNumberOfViruses();
                    }
                }
            }
        }

        return sum;
    }

    private Base checkDeff(ArrayList<Base> arg0, ArrayList<Virus> arg1){
        int count = 0;
        Base origin = null;

        for (Base anArg0 : arg0) {
            if (anArg0.getOwner() == EAlignment.Friendly) {
                if (count < getDmg(anArg0, arg1)) {
                    origin = anArg0;
                    count = getDmg(anArg0, arg1);
                }
            }
        }
        return origin;
    }

    private int getAttackAmount(Base origin, Base enemy, ArrayList<Virus> arg1, GameInformation arg2) {
        int MyAmount = origin.getNumberOfViruses() - getDmg(origin, arg1);
        int distance = arg2.getDistanceBetweenBases(origin, enemy);
        int sendAmount = enemy.getNumberOfViruses() + distance * enemy.getCurProductionLevel();
        if (MyAmount - sendAmount > 2) {
            return sendAmount+1;
        }

        if (enemy.getOwner() == EAlignment.Neutral) {
            return enemy.getNumberOfViruses() + 1;
        }

        return MyAmount;
    }

	@Override
	public Order think(ArrayList<Base> arg0, ArrayList<Virus> arg1, GameInformation arg2) {
		Base origin = null;
		Base target = null;
		Base targetD;
		int count = 0;
		float dist;

        BaseDefend = checkDeff(arg0, arg1);

        for (Base anArg0 : arg0) {
            if (anArg0.getOwner() == EAlignment.Friendly) {
                if (count < anArg0.getNumberOfViruses() - getDmg(anArg0, arg1) && BaseDefend!= anArg0) {
                    if(getDmg(anArg0, arg1) <= anArg0.getNumberOfViruses()) {
                        origin = anArg0;
                        count = anArg0.getNumberOfViruses() - getDmg(anArg0, arg1);
                    }
                }
            }
        }

		if(BaseDefend != null && checkDist(origin, BaseDefend, 4, arg2) && BaseDefend != origin){
		    if(getDmg(BaseDefend, arg1) > 0) {
                return new Order(origin, BaseDefend, getDmg(BaseDefend, arg1));
            }
        }

		if(origin == null) {
            return Order.Idle();
        }

        if(origin.getCurProductionLevel() < 2 && checkUp(origin, arg0, arg2)){
            if(origin.getNumberOfViruses() > arg2.getUpgradeCost(origin.getCurProductionLevel())) {
                return Order.Upgrade(origin);
            }
        }

		while(target==null) {
		    target = searchTarget(origin, arg0, arg2);
            if (target.getNumberOfViruses() < isTarget(target, arg1)) {
                target = null;
            }
        }

        if(arg2.getDistanceBetweenBases(origin, target) > 3){
		    dist = origin.getPosition().distance(target.getPosition())/2;
            targetD = searchNeutral(origin, arg0, dist);
            if(targetD != null){
                target = targetD;
            }
        }

        int amount = getAttackAmount(origin, target, arg1, arg2);
        return new Order(origin, target, amount);
	}

}
