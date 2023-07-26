package com.caesar.bean.tanks;


/**
 *
 */
public class Tank_B extends Tank{

    public static final int DEFAULT_HP = 225;
    private static final int DEFAULT_SPEED = 5;
    private long firedTimeA = 0L;
    private long firedTimeB = 0L;

    public Tank_B(int x, int y, int color, boolean isEnemy, int index) {
        super(x, y, color, Tank.TANK_TYPE_B, DEFAULT_HP, DEFAULT_SPEED, isEnemy, index);
    }

    private boolean attackAbleA(){
        return System.currentTimeMillis() - firedTimeA > FIRE_INTERVAL;
    }

    private boolean attackAbleB(){
        return System.currentTimeMillis() - firedTimeB > FIRE_INTERVAL;
    }

    @Override
    public void explode(int atk) {
        //受到攻击时百分之十的概率完全免伤
        if(Math.random() < 0.1) atk = 0;
        super.explode(atk);
    }

    @Override
    public void attack() {
        if(attackAbleA() || attackAbleB()){
            fire();
            if(attackAbleA()){
                firedTimeA = System.currentTimeMillis();
            }else {
                firedTimeB = System.currentTimeMillis();
            }
        }
    }
}
