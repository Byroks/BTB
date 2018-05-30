import java.util.ArrayList;

import Game.Controller.EAlignment;
import Game.Controller.IPlayerController;
import Game.Logic.Base;
import Game.Logic.GameInformation;
import Game.Logic.Order;
import Game.Logic.Virus;

public class Botto
    implements IPlayerController {
    private GameInformation _gi = null;
    private Base _hq = null;

    private ArrayList<Virus> _prev_map = new ArrayList<Virus>();
    private ArrayList<Movement> _mvmt_list = new ArrayList<Movement>();

    private final boolean debug = false;
    private final double _weight_value = .01;
/** these factors were determined empirically to be a good starting point... */
    private int _dist_weight  = 70;
    private int _troop_weight = 30;
/* ======================================================================= */
    enum DecisionBasis {
        DISTANCE,
        TROOPS
    }
/* ——————————————————————————————————————————————————————————————————————— */
    private DecisionBasis
        currentDecisionBasis()
    {
        return _dist_weight >= _troop_weight ? DecisionBasis.DISTANCE
                                             : DecisionBasis.TROOPS;
    }
/* ======================================================================= */
    class Movement {
        Virus _troop  = null;
        DecisionBasis _based_on;

        public
            Movement(Virus troop, DecisionBasis based_on)
        {
            _troop    = troop;
            _based_on = based_on;
        }
/* ——————————————————————————————————————————————————————————————————————— */
        private void
            outputWeights()
        {
            System.out.println("Distance: " + _dist_weight
                             + "Troop:    " + _troop_weight);
        }
/* ——————————————————————————————————————————————————————————————————————— */
        private void
            incDistanceWeight()
        {
            ++_dist_weight;
            --_troop_weight;
        }
/* ——————————————————————————————————————————————————————————————————————— */
        private void
            incTroopWeight()
        {
            --_dist_weight;
            ++_troop_weight;
        }
/* ——————————————————————————————————————————————————————————————————————— */
        public void
            evaluate()
        {
            if (_troop.getTarget().getOwner() == EAlignment.Friendly) {
                // captured target
                if (_based_on == DecisionBasis.DISTANCE) {
                    incDistanceWeight();
                } else {
                    incTroopWeight();
                }
            } else {
                if (_based_on == DecisionBasis.DISTANCE) {
                    incTroopWeight();
                } else {
                    incDistanceWeight();
                }
            }
            if (debug) {
                outputWeights();
            }
        }
    }
/* ======================================================================= */
    public
        Botto() {}
/* ======================================================================= */
    @Override
    public String
        getName()
    {
        return "ToBotto";
    }
/* ======================================================================= */
    @Override
    public String
        getAuthor()
    {
        return "Tobias Ehlert"; /* auch bekannt als: 219505 */
    }
/* ======================================================================= */
/** provide a more c-like interface for random numbers                     */
    private int
        rand()
    {
        return (int)(Integer.MAX_VALUE * Math.random());
    }
/* ======================================================================= */
    private Base
        targetMinDistance(Base from,
                          ArrayList<Base> to_list)
    {
        Base result = null;
        double min = Double.MAX_VALUE;

        for (Base to : to_list) {
            double v = _gi.getDistanceBetweenBases(from, to);
            min = v <= min ? v : min;
            result = min == v ? to : result;
        }
        return result;
    }
/* ======================================================================= */
    private Base
        targetMinTroops(ArrayList<Base> bases)
    {
        Base result = null;
        int min = Integer.MAX_VALUE;

        for (Base b : bases) {
            int units = b.getNumberOfViruses();
            if (units <= min) {
                result = b;
                min = units;
            }
        }
        return result;
    }
/* ======================================================================= */
    private Base
        targetMinWeighted(Base from,
                          ArrayList<Base> to_list,
                          double weight_distance,
                          double weight_troops)
    {
        Base result = null;
        double min_weight = Double.MAX_VALUE;

        for (Base to : to_list) {
            int units = to.getNumberOfViruses() - from.getNumberOfViruses();
            double plvl = to.getCurProductionLevel();
            plvl = plvl != 0 ? plvl : 1;
            double v = _gi.getDistanceBetweenBases(from, to);

            double weight = (v * weight_distance + units * weight_troops)/plvl;
            if (weight <= min_weight) {
                result = to;
                min_weight = weight;
            }
        }
        return result;
    }
/* ======================================================================= */
    private Base
        baseWithMaxTroops(ArrayList<Base> bases)
    {
        Base result = null;
        int max = Integer.MIN_VALUE;

        for (Base b : bases) {
            int units = b.getNumberOfViruses();
            if (units >= max) {
                result = b;
                max = units;
            }
        }
        return result;
    }
/* ======================================================================= */
    private int
        calcCorpsSize(Base from,
                      Base to,
                      int  extra_troops)
    {
        double distance = _gi.getDistanceBetweenBases(from, to);
        int req_troops = (int)(Math.ceil(distance))
                       + to.getNumberOfViruses()
                       + extra_troops + 1;
        int avl_troops = from.getNumberOfViruses();
        return req_troops > avl_troops ? req_troops : avl_troops;
    }
/* ======================================================================= */
    private boolean
        baseCanUpgrade(Base base)
    {
        return base.getNumberOfViruses()
            >= _gi.getUpgradeCost(base.getCurProductionLevel());
    }
/* ======================================================================= */
    private void
        evaluateTroopMovements(ArrayList<Virus> troops)
    {
        ArrayList<Virus> own_troops = new ArrayList<Virus>();

        for (Virus v : troops)
            if (v.getOwner() == EAlignment.Friendly)
                own_troops.add(v);

        for (Virus v : own_troops)
            if (!_prev_map.contains(v)) // spawned new troop
                _mvmt_list.add(new Movement(v, currentDecisionBasis()));

        for (Virus v : _prev_map) {
            if (!own_troops.contains(v)) { // troop reached target
                int i = 0;
                /* find out what kind of order this troop got */
                for (Movement m : _mvmt_list) {
                    if (m._troop.equals(v))
                        break;
                    ++i;
                }
                _mvmt_list.get(i).evaluate();
                _mvmt_list.remove(i);
            }
        }
        _prev_map = own_troops;
    }
/* ——————————————————————————————————————————————————————————————————————— */
    @Override
    public Order
        think(ArrayList<Base> bases,
              ArrayList<Virus> troops,
              GameInformation gameInfo)
    {
        _gi = gameInfo;
        Base origin = null;
        Base target = null;

        evaluateTroopMovements(troops);

        ArrayList<Base> own_bases = new ArrayList<Base>();
        ArrayList<Base> not_own_bases = new ArrayList<Base>();

        for (Base b : bases) {
            if (b.getOwner() == EAlignment.Friendly)
                own_bases.add(b);
            else if (b.getOwner() == EAlignment.Neutral)
                not_own_bases.add(b);
            else
                not_own_bases.add(b);
        }
        if (own_bases.size() == 0)
            return Order.Idle();

        if (_hq == null)
            _hq = own_bases.get(rand() % own_bases.size());
        if (baseCanUpgrade(_hq))
            return Order.Upgrade(_hq);

        origin = baseWithMaxTroops(own_bases);
        if (_hq.getOwner() != EAlignment.Friendly)
            target = _hq;
        if (not_own_bases.size() != 0)
            target = targetMinWeighted(origin,
                                       not_own_bases, 
                                       _dist_weight * _weight_value, 
                                       _troop_weight * _weight_value);
        else
            return Order.Idle();

        return new Order(origin,
                         target,
                         calcCorpsSize(origin, target, 5));
    }
}
