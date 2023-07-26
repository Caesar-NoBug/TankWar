package com.caesar.bean.blocks;



import com.caesar.bean.common.Explode;
import com.caesar.dao.Dao;

import static com.caesar.util.Constant.*;

public class NormalWall extends Block {

    private static final int[] HP = {175, 350};
    private int hp;

    public NormalWall(int x, int y, int blockType) {
        super(x, y, BLOCK_IMAGES[blockType]);
        this.hp = HP[blockType];
    }


    @Override
    public void explode(int atk) {
        int x = getX() + BLOCK_SIZE / 2, y = getY() + BLOCK_SIZE / 2;
        hp -= atk;
        int explodeType;
        if(hp > 0) explodeType = Explode.SMALL_EXPLODE;
        else{
            explodeType = Explode.BIG_EXPLODE;
            Dao.objectsToRemove.add(this);
        }
        Dao.explodes.add(new Explode(x, y, explodeType));
    }
}
