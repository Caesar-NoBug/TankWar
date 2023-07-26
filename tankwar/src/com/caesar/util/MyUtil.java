package com.caesar.util;

import com.caesar.bean.Base;
import com.caesar.bean.common.Bullet;
import com.caesar.bean.common.Direction;
import com.caesar.bean.blocks.Block;
import com.caesar.bean.tanks.Tank;

import javax.imageio.ImageIO;
import java.awt.*;
import static com.caesar.util.Constant.*;
import java.io.IOException;

/**
 * @author caesar
 */
public class MyUtil {

    public static Image getImage(String path, int width, int height){
        Image image = null;
        try {
            image = ImageIO.read(MyUtil.class.getClassLoader().getResource(path)).getScaledInstance(width, height, Image.SCALE_DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static Image getImage(String path, Dimension dimension){
        int width = dimension.width, height = dimension.height;
        return getImage(path, width, height);
    }

    public static Dimension getTankDimension(int tankType, Direction direction){
        int index;
        if(direction.ordinal() < 2) index = 0;
        else index = 1;
        int width = TANK_SIZES[tankType][index];
        int height = TANK_SIZES[tankType][1 - index];
        return new Dimension(width, height);
    }

    public static Dimension getBulletDimension(Direction direction){
        if(direction.ordinal() < 2) return new Dimension(BULLET_WIDTH, BULLET_HEIGHT);
        else return new Dimension(BULLET_HEIGHT, BULLET_WIDTH);
    }

    public static boolean isCollision(Base base1, Base base2){
        Rectangle rectangle1 = base1.getRectangle();
        Rectangle rectangle2 = base2.getRectangle();
        return rectangle1.intersects(rectangle2);
    }

    public static Dimension getDimension(Base base){
        if(base instanceof Tank){
            Tank tank = (Tank) base;
            return getTankDimension(tank.getTankType(), tank.getDirection());
        }
        else if(base instanceof Bullet){
            Bullet bullet = (Bullet) base;
            return getBulletDimension(bullet.getDirection());
        }
        else if(base instanceof Block){
            return new Dimension(BLOCK_SIZE, BLOCK_SIZE);
        }
        else {
            throw new IllegalStateException("Wrong class :" + base.getClass());
        }
    }

    public static int getRandomNumber(int min, int max){
        return (int)(Math.random() * (max - min) + min);
    }

}
