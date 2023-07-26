package com.caesar.bean.blocks;

import com.caesar.bean.Base;
import com.caesar.bean.common.Explodeable;

import java.awt.*;

public abstract class Block extends Base implements Explodeable {

    /**
     * 地图块依次为：
     * MEDIUM_WALL: 普通的墙
     * TOUGHER_WALL: 坚固的墙
     * HARD_WALL: 无法击毁的墙
     * BUSH: 草丛
     * HOME: 基地
     */
    public static final int MEDIUM_WALL = 0;
    public static final int TOUGHER_WALL = 1;
    public static final int HARD_WALL = 2;
    public static final int BUSH = 3;
    //因基地类有特有属性，故无法直接从此处创建实例对象
    public static final int HOME = 4;

    protected Block(int x, int y, Image image) {
        super(x, y, image);
    }

    public static Block createBlock(int x, int y, int blockType){
        Block block = null;
        switch (blockType) {
            case MEDIUM_WALL:
            case TOUGHER_WALL:
                block = new NormalWall(x, y, blockType);
                break;
            case HARD_WALL:
                block = new HardWall(x, y);
                break;
            case BUSH:
                block = new Bush(x, y);
                break;
            default:
                throw new IllegalStateException("Wrong block type :" + blockType);
        }
        return block;
    }

}
