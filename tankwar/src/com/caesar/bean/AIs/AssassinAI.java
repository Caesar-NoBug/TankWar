package com.caesar.bean.AIs;

import com.caesar.bean.common.Direction;
import com.caesar.bean.tanks.Tank;
import com.caesar.dao.Dao;

import java.awt.*;
import java.util.ArrayList;

/**
 * 此AI会寻找玩家基地并规划路径并进行攻击
 */
public class AssassinAI extends Intelligent {

    private ArrayList<Direction> directions = null;
    private int index;
    private Point goalPoint;


    private void init(Tank tank){
        directions = accessHomeDirection(tank);
        int y = tank.getCenter().y;
        index = directions.size() - 1;
        goalPoint = Dao.playerHome.getCenter();
    }

    @Override
    public void makeDecision(Tank tank) {
        if(noEnemyLeft()) return;
        if(directions == null) init(tank);
        if(index >= 0 && !isConflictToNormalWall(tank.nextRectangle(directions.get(index)))){
            tank.setStanding(false);
            tank.setDirection(directions.get(index));
            index --;
        }
        else{
            tank.setStanding(true);
            Point point = tank.getCenter();
            Direction direction = getAttackDirection(point.x, point.y, goalPoint.x, goalPoint.y);
            if (direction != null)
                tank.setDirection(direction);
            tank.attack();
        }
    }
}
