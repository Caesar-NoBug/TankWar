package com.caesar.bean.common;

import com.caesar.bean.Base;
import com.caesar.bean.blocks.Block;
import com.caesar.bean.blocks.Bush;
import com.caesar.bean.blocks.Home;
import com.caesar.bean.tanks.Tank;
import com.caesar.dao.Dao;
import com.caesar.util.MyUtil;

import java.awt.*;

import static com.caesar.util.Constant.*;

/**
 * @author caesar
 */
public class Bullet extends Base implements Explodeable {

    private int atk;
    private Direction direction;
    private int speed;
    private boolean isEnemy;
    private static final int[] MIN_ATK = {40, 25, 15};
    private static final int[] MAX_ATK = {90, 50, 35};
    public static final int[] SPEED = {30, 20, 10};
    public static final int QUICKER_BULLET = 0;
    public static final int MEDIUM_BULLET = 1;
    public static final int SLOWER_BULLET = 2;


    private Bullet(int x, int y, int minAtk, int maxAtk, Direction direction, int speed, boolean isEnemy) {
        super(x, y, BULLET_IMAGES[direction.ordinal()]);
        this.atk = MyUtil.getRandomNumber(minAtk, maxAtk);
        this.direction = direction;
        this.speed = speed;
        this.isEnemy = isEnemy;
    }

    public static Bullet createBullet(int x, int y, int bulletType, Direction direction, boolean isEnemy){
         return new Bullet(x, y, MIN_ATK[bulletType], MAX_ATK[bulletType],
                 direction, SPEED[bulletType], isEnemy);
    }

    @Override
    public void paint(Graphics g) {
        move();
        super.paint(g);
    }

    /**
     * @return 如果存在碰撞，返回碰撞到的对象，否则返回null
     */
    private Explodeable hasCollision(){

        //判断是否与地图块碰撞
        for(Block block : Dao.blocks){
            //不攻击己方基地和草丛
            if(block instanceof Home && ((Home) block).isEnemy() == this.isEnemy) continue;
            if(!(block instanceof Bush) && MyUtil.isCollision(this, block))
                return block;
        }

        for (Bullet bullet : Dao.bullets) {
            if(isEnemy() != bullet.isEnemy() && MyUtil.isCollision(this, bullet))
                return bullet;
        }

        //判断是否与坦克碰撞
        if(isEnemy){
            if (Dao.playerOne.isAlive() && MyUtil.isCollision(this, Dao.playerOne.getTank()))
                return Dao.playerOne.getTank();
            else if(Dao.playerCount == 2 && Dao.playerTwo.isAlive() &&MyUtil.isCollision(this, Dao.playerTwo.getTank()))
                return Dao.playerTwo.getTank();
        }
        else {
            for (Tank enemyTank : Dao.computerTanks) {
                if(MyUtil.isCollision(this, enemyTank))
                    return enemyTank;
            }
        }

        return null;
    }

    private void move(){
        int x = getX(), y = getY();
        switch (direction) {
            case UP -> {
                setDirection(Direction.UP);
                y -= speed;
            }
            case DOWN -> {
                setDirection(Direction.DOWN);
                y += speed;
            }
            case LEFT -> {
                setDirection(Direction.LEFT);
                x -= speed;
            }
            case RIGHT -> {
                setDirection(Direction.RIGHT);
                x += speed;
            }
            default -> throw new IllegalArgumentException("" + direction);
        }
        setX(x); setY(y);
        Explodeable explodeObject = hasCollision();
        if(explodeObject != null){
            Dao.objectsToRemove.add(this);
            explodeObject.explode(getAtk());
        }

    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getAtk() {
        return atk;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public void setEnemy(boolean enemy) {
        isEnemy = enemy;
    }

    @Override
    public void explode(int atk) {
        Dimension dimension = MyUtil.getBulletDimension(direction);
        int x = getX() + dimension.width / 2, y = getY() + dimension.height / 2;
        Dao.explodes.add(new Explode(x, y, Explode.SMALL_EXPLODE));
    }

}
