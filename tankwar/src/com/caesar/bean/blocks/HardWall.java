package com.caesar.bean.blocks;

import com.caesar.bean.common.Explode;
import com.caesar.dao.Dao;

import static com.caesar.util.Constant.*;

public class HardWall extends Block {

    public HardWall(int x, int y) {
        super(x, y, BLOCK_IMAGES[Block.HARD_WALL]);
    }

    @Override
    public void explode(int atk) {
        int x = getX() + BLOCK_SIZE / 2, y = getY() + BLOCK_SIZE / 2;
        Dao.explodes.add(new Explode(x, y, Explode.SMALL_EXPLODE));
    }
}
