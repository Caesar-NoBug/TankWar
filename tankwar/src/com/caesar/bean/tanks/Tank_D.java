package com.caesar.bean.tanks;

import com.caesar.bean.common.Bullet;

public class Tank_D extends Tank{

    public static final int DEFAULT_HP = 175;
    private static final int DEFAULT_SPEED = 6;

    public Tank_D(int x, int y, int color, boolean isEnemy, int index) {
        super(x, y, color, Tank.TANK_TYPE_D, DEFAULT_HP, DEFAULT_SPEED, isEnemy, index);
    }

    @Override
    public void attack() {
        if(attackAble()) {
            Bullet bullet = fire();
            setFiredTime(System.currentTimeMillis());
            //发射子弹后有一定概率伤害翻倍
            if(Math.random() < 0.15){
                bullet.setAtk(bullet.getAtk() << 1);
            }
        }
    }
}
