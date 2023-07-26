package com.caesar.bean;

import com.caesar.bean.common.Direction;
import com.caesar.bean.tanks.Tank;
import com.caesar.dao.Dao;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Tank的装饰类，代表玩家，用于与用户交互
 */
public class Player {
    private int index;
    private Tank tank;
    private boolean isReborn = false;
    private boolean isAlive = true;
    public Player(int x, int y, int color, int tankType, int index) {
        this.tank = Tank.createPlayerTank(x, y, color, tankType, index);
        this.index = index;
    }

    public void keyPressedEvent(int keyCode) {
        tank.setStanding(false);
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                tank.setDirection(Direction.UP);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                tank.setDirection(Direction.DOWN);
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                tank.setDirection(Direction.LEFT);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                tank.setDirection(Direction.RIGHT);
                break;
        }
    }

    /**
     * 尝试是否需要复活，如果需要则设置rebornAble为true
     */
    private void tryReborn(){
        if(tank == null) return;
        if(tank.getHp() <= 0){
            if(index == 1 && Dao.playerOneLife > 0){
                if(-- Dao.playerOneLife > 0) {
                    isReborn = true;
                }
                else {
                    isAlive = false;
                    tank = null;
                }
            }
            else if(index == 2 && Dao.playerTwoLife > 0){
                if(-- Dao.playerTwoLife > 0)
                    isReborn = true;
                else {
                    isAlive = false;
                    tank = null;
                }
            }
        }else {
            isReborn = false;
        }
    }
    public void paint(Graphics g){
        tryReborn();
        if(isAlive) tank.paint(g);
    }

    public void keyReleasedEvent(int keyCode) {
        tank.setStanding(true);
        if(keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER)
            tank.attack();

    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isReborn() {
        return isReborn;
    }

    public Tank getTank() {
        return tank;
    }

    public void setTank(Tank tank) {
        this.tank = tank;
    }
}
