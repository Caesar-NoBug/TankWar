package com.caesar.bean.tanks;

import com.caesar.bean.AIs.Intelligent;
import com.caesar.bean.Base;
import com.caesar.bean.blocks.Block;
import com.caesar.bean.blocks.Bush;
import com.caesar.bean.common.*;
import com.caesar.util.MyUtil;

import static com.caesar.dao.Dao.*;
import static com.caesar.util.Constant.*;
import java.awt.*;

/**
 * @author caesar
 */
public class Tank extends Base implements Explodeable {

    public static final int TANK_TYPE_A = 0;
    public static final int TANK_TYPE_B = 1;
    public static final int TANK_TYPE_C = 2;
    public static final int TANK_TYPE_D = 3;

    public static final int BLUE = 0;
    public static final int RED = 1;

    private static final int[] dx = {0, 0, -1, 1, 0};
    private static final int[] dy = {-1, 1, 0, 0, 0};

    private int color;
    private int tankType;
    private int bulletType;
    private int hp;
    private int speed;
    private Direction direction = Direction.UP;
    private boolean standing = true;//默认为静止状态
    private boolean isComputer;
    private long firedTime = 0L;
    //坦克半径
    private int radius;
    private Intelligent ai;
    private int index;
    private BloodBar bloodBar;
    /**
     * 最小攻击间隔
     */
    protected static final int FIRE_INTERVAL = 500;

    protected Tank(int x, int y, int color, int tankType, int hp, int speed, boolean isComputer, int index) {
        super(x, y, TANKS_IMAGE[color][tankType][Direction.UP.ordinal()]);
        if(tankType == TANK_TYPE_D) bulletType = Bullet.QUICKER_BULLET;
        else if(tankType == TANK_TYPE_C) bulletType = Bullet.SLOWER_BULLET;
        else bulletType = Bullet.MEDIUM_BULLET;
        this.color = color;
        this.tankType = tankType;
        this.hp = hp;
        this.speed = speed;
        this.isComputer = isComputer;
        Dimension dimension = MyUtil.getTankDimension(tankType, direction);
        this.radius = dimension.width + dimension.height >> 2;
        this.index = index;
        this.bloodBar = new BloodBar(index);
    }

    /**
     * 此方法直接用于创建坦克
     * @param x x坐标
     * @param y y坐标
     * @param color 坦克颜色
     * @param tankType 坦克型号
     * @param isComputer 是否是电脑
     * @param index 玩家编号
     * @param ai 控制坦克的AI
     * @return 创建的坦克
     */
    private static Tank createTank(int x, int y, int color, int tankType, boolean isComputer, int index, Intelligent ai){
        Tank tank = null;
        switch (tankType){
            case TANK_TYPE_A -> tank = new Tank_A(x, y, color, isComputer, index);
            case TANK_TYPE_B -> tank = new Tank_B(x, y, color, isComputer, index);
            case TANK_TYPE_C -> tank = new Tank_C(x, y, color, isComputer, index);
            case TANK_TYPE_D -> tank = new Tank_D(x, y, color, isComputer, index);
            default -> new IllegalStateException("Illegal tankType :" + tankType);
        }
        tank.ai = ai;
        return tank;
    }
    /**
     * 此方法用于创建玩家的坦克
     */
    public static Tank createPlayerTank(int x, int y, int color, int tankType, int index){
        return createTank(x, y, color, tankType, false, index, null);
    }
    /**
     * 此方法用于创建电脑的坦克
     */
    public static Tank createComputerTank(int x, int y, int color, int tankType, Intelligent ai){
        return createTank(x, y, color, tankType, true, BloodBar.INDEX_COMPUTER, ai);
    }

    public Point getCenter(){
        return new Point(getX() + radius, getY() + radius);
    }

    /**
     * @return 坦克炮口所在位置
     */
    public Point getHead(){
        Dimension dimension = MyUtil.getTankDimension(tankType, direction);
        int width = dimension.width, height = dimension.height;
        int index = direction.ordinal();
        if(index < 2)  return new Point(getX() + width * 2 / 5, getY() + height * index);
        else return new Point(getX() + width * (index - 2), getY() + height * 2 / 5);
    }

    public void setFiredTime(long firedTime) {
        this.firedTime = firedTime;
    }

    public int getBulletSpeed() {return Bullet.SPEED[bulletType];}

    public int getTankType() {
        return tankType;
    }

    public void setStanding(boolean standing) {
        this.standing = standing;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setIntelligent(Intelligent ai){this.ai = ai;}

    public void setDirection(Direction direction) {
        this.direction = direction;
        Image image = TANKS_IMAGE[color][tankType][direction.ordinal()];
        this.setImage(image);
    }

    public boolean isComputer() {
        return isComputer;
    }

    @Override
    public void paint(Graphics g) {
        if(isComputer){
            ai.makeDecision(this);
        }
        bloodBar.paint(g);
        move();
        super.paint(g);
    }

    @Override
    public Rectangle getRectangle() {
        return new Rectangle(getX(), getY(),
                radius * 2, radius * 2);
    }

    private boolean isCollideToBlock(Rectangle rectangle){
        for (Block block : blocks) {
            if(!(block instanceof Bush) && rectangle.intersects(block.getRectangle())) {
                return true;
            }
        }
        return false;
    }

    private void move(){

        int x = getX(), y = getY();

        if(isCollideToBlock(nextRectangle(direction))) return;
        int diameter = radius << 1;
        if(!standing) {
            switch (direction) {
                case UP -> {
                    setDirection(Direction.UP);
                    y -= speed;
                    if (y < 0) {
                        y = 0;
                    }
                }
                case DOWN -> {
                    setDirection(Direction.DOWN);
                    y += speed;
                    if (y + diameter > PANE_HEIGHT) {
                        y = PANE_HEIGHT - diameter;
                    }
                }
                case LEFT -> {
                    setDirection(Direction.LEFT);
                    x -= speed;
                    if (x < 0) {
                        x = 0;
                    }
                }
                case RIGHT -> {
                    setDirection(Direction.RIGHT);
                    x += speed;
                    if (x + diameter > PANE_WIDTH) {
                        x = PANE_WIDTH - diameter;
                    }
                }
                default -> throw new IllegalArgumentException("" + direction);
            }
        }
        setX(x); setY(y);
    }

    public Rectangle nextRectangle(Direction direction){
        int directionIndex = direction.ordinal();
        return new Rectangle(getCenter().x + dx[directionIndex] * speed - radius,
                getCenter().y + dy[directionIndex] * speed - radius, radius << 1, radius << 1);
    }

    protected boolean attackAble(){
        return System.currentTimeMillis() - firedTime > FIRE_INTERVAL;
    }

    public void attack() {
        if(attackAble()) {
            fire();
            firedTime = System.currentTimeMillis();
        }
    }

    protected Bullet fire(){
        Point point = getHead();
        int x = point.x, y = point.y;
        Bullet bullet = Bullet.createBullet(x, y, bulletType, getDirection(), isComputer());
        bullets.add(bullet);
        return bullet;
    }

    @Override
    public void explode(int atk) {
        Point point = getCenter();
        int x = point.x, y = point.y;
        hp -= atk;
        int explodeType;
        if(hp > 0) explodeType = Explode.SMALL_EXPLODE;
        else {
            explodeType = Explode.BIG_EXPLODE;
            if(isComputer){
                objectsToRemove.add(this);
            }
        }
        explodes.add(new Explode(x, y, explodeType));
    }

    private class BloodBar{
        //血条的长、宽
        private static final int BAR_WIDTH = 50;
        private static final int BAR_HEIGHT = 7;
        //坦克的编号：电脑为0，玩家一为1，玩家二为2
        private int index;
        public static final int INDEX_COMPUTER = 0;
        //默认血量（满血）
        private int HP;

        private BloodBar(int index){
            this.index = index;
            if(index < 0 || index > 2)
                throw new IllegalStateException("wrong index in BloodBar : " + index);
            switch (tankType){
                case TANK_TYPE_A -> HP = Tank_A.DEFAULT_HP;
                case TANK_TYPE_B -> HP = Tank_B.DEFAULT_HP;
                case TANK_TYPE_C -> HP = Tank_C.DEFAULT_HP;
                case TANK_TYPE_D -> HP = Tank_D.DEFAULT_HP;
            }
        }

        public void paint(Graphics g){
            double ratio = (double) hp / HP;
            Color color = null;
            if(index == 0){
                color = Color.RED;
            }
            else {
                int life = index == 1 ? playerOneLife : playerTwoLife;
                if(life >= 3) color = Color.GREEN;
                else if(life == 2) color = Color.YELLOW;
                else color = Color.RED;
            }
            g.setColor(color);
            g.fillRect(getX(), getY() - BAR_HEIGHT, (int) (BAR_WIDTH * ratio), BAR_HEIGHT);

            //绘制白色血条边框
            g.setColor(Color.WHITE);
            g.drawRect(getX() , getY() - BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

    }
}
