package com.caesar.bean;

import com.caesar.bean.AIs.AssassinAI;
import com.caesar.bean.AIs.KnightAI;
import com.caesar.bean.AIs.Intelligent;
import com.caesar.bean.tanks.Tank;

import java.awt.*;
/**
 * Tank的装饰类，代表敌人，用于添加AI
 * (骑士：无畏生死，以杀敌为目标；
 *  刺客：偷袭，以攻占地方基地为目标；
 *  守卫：阻击敌方的攻击，以保卫基地为目标；
 *  )
 */
public class Computer {

    private Tank tank;

    public Computer(int x, int y, int color, int tankType){
        Intelligent ai = null;
        //1/3的概率生成攻击基地的AI，2/3的概率生成攻击玩家的AI
        if(Math.random() < 0.33333) ai = new AssassinAI();
        else ai = new KnightAI();
        this.tank = Tank.createComputerTank(x, y, color, tankType, ai);
    }

    public Tank getTank() {
        return tank;
    }

    public void setTank(Tank tank) {
        this.tank = tank;
    }


}
