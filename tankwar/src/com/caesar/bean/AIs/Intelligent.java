package com.caesar.bean.AIs;

import com.caesar.bean.common.Direction;
import com.caesar.bean.blocks.Block;
import com.caesar.bean.blocks.HardWall;
import com.caesar.bean.blocks.NormalWall;
import com.caesar.bean.tanks.Tank;
import com.caesar.dao.Dao;
import com.caesar.util.Constant;
import com.caesar.util.MyUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;

import static com.caesar.util.Constant.TANK_SIZES;

public abstract class Intelligent {

    public abstract void makeDecision(Tank tank);
    /**
     * 5个选择：上、下、左、右、不动
     */
    private static final int DECISION_COUNT = 5;
    private static final int[] dx = {0, 0, -1, 1, 0};
    private static final int[] dy = {-1, 1, 0, 0, 0};

    //坦克碰撞半径及地图最大路径长度
    private static final int MAP_MAX_DIST = 900 * 900;
    private static final int JUDGE_DISTANCE = Constant.BULLET_WIDTH * 3 / 2;
    /**
     * @param tank AI控制的坦克
     * @return 离坦克曼哈顿距离最近的玩家坦克的坐标
     */
    protected Point getClosestPlayerTankPoint(Tank tank){
        //坦克自己的位置信息
        Point point0 = tank.getCenter();
        int x0 = point0.x, y0 = point0.y;
        //玩家一的位置信息
        Point point1 = new Point(0, 0);
        Point point2 = new Point(0, 0);
        int d1 = Integer.MAX_VALUE;
        int d2 = Integer.MAX_VALUE;
        if(Dao.playerOne.isAlive()) {
            point1 = Dao.playerOne.getTank().getCenter();
            int x1 = point1.x, y1 = point1.y;
            d1 = Math.abs(x0 - x1) + Math.abs(y0 - y1);
        }
        //玩家二的位置信息
        if(Dao.playerCount == 2 && Dao.playerTwo.isAlive()) {
            point2 = Dao.playerTwo.getTank().getCenter();
            int x2 = point2.x, y2 = point2.y;
            d2 = Math.abs(x0 - x2) + Math.abs(y0 - y2);
        }
        //返回曼哈顿距离最近的地方坦克的坐标
        if(d1 <= d2) return point1;
        else return point2;
    }

    /**
     * @param startX 起始x坐标
     * @param startY 起始y坐标
     * @param goalX 目标x坐标
     * @param goalY 目标y坐标
     * @return 返回目标在自己的哪个方向上，如果不在同一直线返回null
     */
    protected Direction getAttackDirection(int startX, int startY, int goalX, int goalY){

        int judgeDistance = Constant.BULLET_WIDTH << 2;

        if(getMinDistance(startX, startY, goalX, goalY) > judgeDistance) return null;
        Direction direction = null;
        if(Math.abs(startX - goalX) <= judgeDistance ){
            if(startY < goalY) direction = Direction.DOWN;
            else if(startY > goalY) direction = Direction.UP;
        }
        else {
            if(startX < goalX) direction = Direction.RIGHT;
            else direction = Direction.LEFT;
        }
        return direction;
    }

    /**
     * @param startX 起始x坐标
     * @param startY 起始y坐标
     * @param goalX 目标x坐标
     * @param goalY 目标y坐标
     * @return
     */
    protected int getMinDistance(int startX, int startY, int goalX, int goalY){
        int d1 = Math.abs(startX - goalX);
        int d2 = Math.abs(startY - goalY);
        return Math.min(d1, d2);
    }

    protected boolean noEnemyLeft(){
        return Dao.playerOneLife <= 0 &&(Dao.playerCount == 2 && Dao.playerTwoLife <= 0);
    }

    /**
     * 用于判断如何最快接近敌方坦克并将其纳入攻击范围（基于贪心算法，可能计算错误，以降低难度）
     * @param tank AI控制的坦克
     * @return 坦克到达 x = goalX 或 y = goalY的最近的路线方向,若无需移动（已到达）则返回null
     */
    protected Direction accessEnemyDirection(Tank tank){
        Point playerPoint = getClosestPlayerTankPoint(tank);
        int goalX = playerPoint.x, goalY = playerPoint.y;
        Point point = tank.getCenter();
        int x = point.x, y = point.y;
        int bestChoice = 0;
        //基于贪心的搜索
        int minDist = Integer.MAX_VALUE, speed = tank.getSpeed();
        for (int i = 0; i < DECISION_COUNT; i++) {
            int d = getMinDistance(x + dx[i] * speed, y + dy[i] * speed, goalX, goalY);
            if(i < DECISION_COUNT - 1 &&
                    !crossAble(tank.nextRectangle(Direction.valueOf(i)), Direction.valueOf(i)))
                continue;
            if(d < minDist) {
                minDist = d;
                bestChoice = i;
            }
            if(minDist <= Constant.BULLET_WIDTH * 2){
                return null;
            }
        }

        if(bestChoice == DECISION_COUNT - 1) return null;
        return Direction.valueOf(bestChoice);
    }

    /**
     * 用于使坦克找到攻击基地的路径
     * @param tank AI坦克
     * @return 用A*算法实现坦克线路规划
     */
    protected ArrayList<Direction> accessHomeDirection(Tank tank){
        Point enemyHomePoint = Dao.playerHome.getCenter();

        int goalX = enemyHomePoint.x, goalY = enemyHomePoint.y;
        Point point = tank.getCenter();

        ArrayList<Direction> directions = new ArrayList<>();
        Queue<SearchPoint> pointsQueue = new PriorityQueue<>();
        //储存已经访问过的点
        HashSet<Point> pointsVisited = new HashSet<>();

        for (int i = 0; i < DECISION_COUNT; i++) {
            int x = point.x, y = point.y;
            int endDist = getMinDistance(x, y, goalX, goalY);
            int startDist = 0;
            if(i < DECISION_COUNT - 1) startDist = 1;
            //初始化队列：加入五个决策的选择
            pointsQueue.add(new SearchPoint(null, i, startDist, endDist,
                    new Point(x + dx[i] * tank.getSpeed(), y + dy[i] * tank.getSpeed())));
            pointsVisited.add(new Point(x + dx[i], y + dy[i]));
        }

        SearchPoint endSearchPoint = null;
        while(pointsQueue.size() > 0){
            SearchPoint sp = pointsQueue.poll();
            if (sp.startDist + sp.endDist > MAP_MAX_DIST) continue;
            int x = sp.point.x, y = sp.point.y;

            //矩形左上角的坐标
            int tx = Math.min(x, goalX), ty = Math.min(y, goalY);
            //判断是否已经搜索完成
            if(Math.abs(x - goalX) <= JUDGE_DISTANCE &&
                    !isConflictToHardWall(new Rectangle(tx - JUDGE_DISTANCE / 2, ty,
                            JUDGE_DISTANCE, Math.abs(y - goalY)))){
                endSearchPoint = sp; break;
            }

            if(Math.abs(y - goalY) <= JUDGE_DISTANCE &&
                    !isConflictToHardWall(new Rectangle(tx, ty - JUDGE_DISTANCE / 2, Math.abs(x - goalX), JUDGE_DISTANCE))){
                endSearchPoint = sp; break;
            }

            int ltDireIdx = -1;
            if(sp.directionIndex != DECISION_COUNT -1)
                ltDireIdx = sp.directionIndex;

            final int DIRECTION_COUNT = 3;
            int[] indexes = new int[DIRECTION_COUNT];
            int idx = sp.directionIndex;
            //优化搜索顺序: 优先搜索与上一步方向相同的分支，极大优化性能
            indexes[0] = idx;
            indexes[1] = ((idx >> 1 & 1) ^ 1) * 2;
            indexes[2] = ((idx >> 1 & 1) ^ 1) * 2 + 1;
            //搜索过程排除不移动的情况，因为这种情况显然会徒劳地增加步数
            for (int i = 0; i < DIRECTION_COUNT; i ++) {
                int j = indexes[i];
                Point point1 = new Point(x + dx[j] * tank.getSpeed(), y + dy[j] * tank.getSpeed());
                if(pointsVisited.contains(point1)) continue;
                int endDist = 0;
                int diameter = TANK_SIZES[tank.getTankType()][0] + TANK_SIZES[tank.getTankType()][1] >> 1;
                Rectangle nextPositionRectangle = new Rectangle(point1.x - diameter / 2, point1.y - diameter / 2,
                        diameter, diameter);
                if (!crossAble(nextPositionRectangle, Direction.valueOf(i)))
                    continue;
                if(isConflictToHardWall(nextPositionRectangle))
                    continue;
                endDist += getMinDistance(x + dx[j], y + dy[j], goalX, goalY);
                pointsQueue.add(new SearchPoint(sp, j, sp.startDist + 1,
                        endDist, point1));
                pointsVisited.add(point1);
            }
        }
        if(endSearchPoint == null) return directions;
        while(endSearchPoint.lastSearchPoint != null){
            directions.add(Direction.valueOf(endSearchPoint.directionIndex));
            endSearchPoint = endSearchPoint.lastSearchPoint;
        }
        return directions;
    }

    /**
     * @param rectangle AI坦克的位置
     * @return 是否会与硬墙碰撞
     */
    protected boolean isConflictToHardWall(Rectangle rectangle){

        for (Block block : Dao.blocks) {
            if(block instanceof HardWall && rectangle.intersects(block.getRectangle()))
                return true;
        }
        return false;
    }

    /**
     * @param rectangle 搜索状态中坦克所处位置
     * @param direction 下一步的方向
     * @return 是否存在能够阻碍坦克且坦克当前位置无法击中该墙的墙
     */
    protected boolean crossAble(Rectangle rectangle, Direction direction){
        int width = MyUtil.getBulletDimension(direction).width;
        int height = MyUtil.getBulletDimension(direction).height;
        int x = (int) rectangle.getCenterX();
        int y = (int) rectangle.getCenterY();
        for (Block block : Dao.blocks) {
            if(block instanceof NormalWall && rectangle.intersects(block.getRectangle())) {
                if(!new Rectangle(x - width / 2, y - height / 2,
                        width, height).intersects(block.getRectangle()))
                    return false;
            }
        }
            return true;
    }

    protected boolean isConflictToNormalWall(Rectangle rectangle){

        for (Block block : Dao.blocks) {
            if(block instanceof NormalWall && rectangle.intersects(block.getRectangle()))
                return true;
        }
        return false;
    }
    /**
     * 用于进行A*算法
     * startDirectionIndex: 最初移动的方向
     * lastDirectionIndex: 上一次移动的方向
     * startDist: 当前点到起点的距离
     * endDist: 当前点到终点的距离
     * point: 当前搜索到的位置
     */
    private class SearchPoint implements Comparable<SearchPoint>{

        public SearchPoint lastSearchPoint;
        public int directionIndex;
        public int startDist;
        public int endDist;
        public Point point;

        @Override
        public String toString() {
            return "SearchPoint{" +
                    "lastSearchPoint=" + lastSearchPoint +
                    ", directionIndex=" + directionIndex +
                    ", startDist=" + startDist +
                    ", endDist=" + endDist +
                    ", point=" + point +
                    '}';
        }

        public SearchPoint(SearchPoint lastSearchPoint, int directionIndex, int startDist, int endDist, Point point) {
            this.lastSearchPoint = lastSearchPoint;
            this.directionIndex = directionIndex;
            this.startDist = startDist;
            this.endDist = endDist;
            this.point = point;
        }

        /**
         * @param sp
         * @return
         */
        @Override
        public int compareTo(SearchPoint sp) {
            int dist1 = this.startDist + this.endDist;
            int dist2 = sp.startDist + sp.endDist;
            int res = 0;
            if(dist1 < dist2) res = -1;
            else if(dist1 > dist2) res = 1;
            return res;
        }
    }

    /**
     * @param tank AI控制的坦克
     * @return 攻击应进行的决策
     */
    protected Decision attackDecision(Tank tank){

        Point goalPoint = getClosestPlayerTankPoint(tank);
        Direction direction = accessEnemyDirection(tank);

        if(direction == null || isConflictToNormalWall(tank.nextRectangle(direction))){
            Point tankPoint = tank.getCenter();
            direction = getAttackDirection(tankPoint.x, tankPoint.y, goalPoint.x, goalPoint.y);
            return new Decision(direction, false, true);
        }

        return new Decision(direction, true, false);
    }

    /**
     * 此方法用于下版本升级AI
     * @param attackTank 攻击方坦克
     * @param attackedTank 被攻击坦克
     * @return 击中目标坦克的概率（ >= 1 表示必定击中）
     */
    protected double hitRate(Tank attackTank, Tank attackedTank) {
        //子弹速度和目标移动速度
        int bulletSpeed = attackTank.getBulletSpeed(), goalSpeed = attackedTank.getSpeed();
        //目标坦克长度的一半,即最小逃逸距离
        int goalRadius = Constant.TANK_SIZES[attackedTank.getTankType()][1] / 2;
        //必定击中的距离
        int absoluteDistance = goalRadius * bulletSpeed / goalSpeed;
        //实际距离
        int actualDistance = getMinDistance(attackTank.getX(), attackTank.getY(), attackedTank.getX(), attackedTank.getY());
        //返回必中距离和实际距离的比值（击中率）
        return absoluteDistance / actualDistance;
    }

    /**
     * 决策包括三个内容：
     * direction: 坦克的朝向
     * isMove 是否移动
     * isAttack: 是否攻击
     */
    protected class Decision{
        public Direction direction;
        public boolean isMove;
        public boolean isAttack;

        public Decision(Direction direction, boolean isMove, boolean isAttack) {
            this.direction = direction;
            this.isMove = isMove;
            this.isAttack = isAttack;
        }
    }
}
