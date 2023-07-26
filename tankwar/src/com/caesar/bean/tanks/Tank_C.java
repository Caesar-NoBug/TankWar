package com.caesar.bean.tanks;

import com.caesar.bean.common.Bullet;
import com.caesar.dao.Dao;

import java.awt.*;

/**
 * C型坦克：具有近战伤害且在近距离受到伤害减半
 */
public class Tank_C extends Tank{

    public static final int DEFAULT_HP = 275;
    private static final int DEFAULT_SPEED = 7;
    private static final int COMBAT_ATK = 175;
    private static final int COMBAT_RADIUS = 10;
    private static final int DEFENCE_RADIUS = 100;

    public Tank_C(int x, int y, int color, boolean isEnemy, int index) {
        super(x, y, color, Tank.TANK_TYPE_C, DEFAULT_HP, DEFAULT_SPEED, isEnemy, index);
    }

    @Override
    public void attack() {
        closeCombat();
        super.attack();
    }

    private void closeCombat(){
        Point headPoint = getHead();
        Rectangle combatArea = new Rectangle(headPoint.x - COMBAT_RADIUS, headPoint.y - COMBAT_RADIUS,
                COMBAT_RADIUS << 1, COMBAT_RADIUS << 1);
        if(encounterEnemy(combatArea) && attackAble()){
            Bullet bullet = fire();
            bullet.setAtk(COMBAT_ATK);
            setFiredTime(System.currentTimeMillis());
        }
    }

    private boolean encounterEnemy(Rectangle area){
        if(isComputer() && (Dao.playerOneLife > 0 && area.intersects(Dao.playerOne.getTank().getRectangle()) ||(
               Dao.playerTwoLife > 0 && area.intersects(Dao.playerTwo.getTank().getRectangle())))) {
            return true;
        }
        else {
            for (Tank tank : Dao.computerTanks) {
                if(area.intersects(tank.getRectangle()))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void explode(int atk) {
        Rectangle defenceArea = new Rectangle(getCenter().x - DEFENCE_RADIUS, getCenter().y - DEFENCE_RADIUS,
                DEFENCE_RADIUS * 2, DEFENCE_RADIUS * 2);
        if(encounterEnemy(defenceArea)) atk >>= 1;
        super.explode(atk);
    }
}
