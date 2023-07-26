package com.caesar.bean.blocks;

import com.caesar.bean.common.Explode;
import com.caesar.dao.Dao;
import com.caesar.util.Constant;

import java.awt.*;

import static com.caesar.util.Constant.BLOCK_SIZE;

public class Home extends Block{
    int hp = 300;
    boolean isEnemy;
    boolean isDestroyed = false;

    public Home(int x, int y, boolean isEnemy) {
        super(x, y, Constant.BLOCK_IMAGES[Block.HOME]);
        this.isEnemy = isEnemy;
    }

    public Point getCenter(){
        return new Point(getX() + BLOCK_SIZE / 2, getY() + BLOCK_SIZE / 2);
    }

    @Override
    public void explode(int atk) {
        int x = getX() + BLOCK_SIZE / 2, y = getY() + BLOCK_SIZE / 2;
        Dao.explodes.add(new Explode(x, y, Explode.BIG_EXPLODE));
        if((hp -= atk) < 0){
            isDestroyed = true;
            Dao.objectsToRemove.add(this);
        }
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
}
