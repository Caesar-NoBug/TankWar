package com.caesar.bean.common;

import com.caesar.bean.Base;
import com.caesar.dao.Dao;

import static com.caesar.util.Constant.*;
import java.awt.*;

public class Explode extends Base {
    private static final int DEFAULT_STATE = 0;

    private int explodeState = DEFAULT_STATE;
    private int FrameExploded = DEFAULT_STATE;

    public static final int BIG_EXPLODE = 8;
    public static final int SMALL_EXPLODE = 2;

    private int maxState;

    /**
     * @param x 爆炸中心点横坐标
     * @param y 爆炸中心点纵坐标
     * @param explodeType
     */
    public Explode(int x, int y, int explodeType) {
        super(x - BLOCK_SIZE / 2, y - BLOCK_SIZE / 2, BOOM_IMAGES[DEFAULT_STATE]);
        this.maxState = explodeType;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        FrameExploded ++;

        if(explodeState == maxState){
            Dao.objectsToRemove.add(this);
        }

        if(FrameExploded % 3 == 0){
            explodeState ++;
            setImage(BOOM_IMAGES[explodeState]);
        }

    }

}
