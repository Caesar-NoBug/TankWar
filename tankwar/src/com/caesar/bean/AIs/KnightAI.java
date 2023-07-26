package com.caesar.bean.AIs;

import com.caesar.bean.tanks.Tank;

/**
 * 此AI会寻找玩家位置并接近玩家并攻击
 */
public class KnightAI extends Intelligent {

    @Override
    public void makeDecision(Tank tank) {
        if (noEnemyLeft()) return;
        Decision atkDecision = attackDecision(tank);
        if(atkDecision.direction != null)
            tank.setDirection(atkDecision.direction);
        tank.setStanding(!atkDecision.isMove);
        if(atkDecision.isAttack)
            tank.attack();
    }

}
