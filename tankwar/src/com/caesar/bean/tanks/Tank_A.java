package com.caesar.bean.tanks;

import java.awt.*;

public class Tank_A extends Tank{

    public static final int DEFAULT_HP = 175;
    private static final int DEFAULT_SPEED = 10;
    private static final int FASTER_SPEED = 13;
    private long lastAttackedTime = 0L;

    public Tank_A(int x, int y, int color, boolean isEnemy, int index) {
        super(x, y, color, Tank.TANK_TYPE_A, DEFAULT_HP, DEFAULT_SPEED, isEnemy, index);
    }

    @Override
    public void explode(int atk) {
        super.explode(atk);
        lastAttackedTime = System.currentTimeMillis();
    }

    @Override
    public void paint(Graphics g) {
        //脱战3秒后获得加速和回复效果
        if(System.currentTimeMillis() - lastAttackedTime > 3000){
            setSpeed(FASTER_SPEED);
            setHp(Math.min(DEFAULT_HP, getHp() + (int) (Math.random() * 2)));
        }else {
            setSpeed(DEFAULT_SPEED);
        }
        super.paint(g);
    }
}
