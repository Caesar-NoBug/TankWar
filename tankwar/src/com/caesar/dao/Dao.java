package com.caesar.dao;

import com.caesar.bean.Base;
import com.caesar.bean.common.Bullet;
import com.caesar.bean.common.Explode;
import com.caesar.bean.blocks.*;
import com.caesar.bean.Player;
import com.caesar.bean.tanks.Tank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 用于存储本局游戏的关卡信息
 */
public class Dao {
    //胜利条件
    public static int gameModel;
    //玩家坦克颜色
    public static int playerColor;
    //玩家数
    public static int playerCount;
    public static Player playerOne;
    public static Player playerTwo;
    //玩家生命数
    public static int playerOneLife;
    public static int playerTwoLife;
    //生成的电脑数
    public static int computerCount = 0;
    //双方的基地
    public static Home playerHome;
    public static Home computerHome;
    //所有地图
    public static ArrayList<Stage> stages = null;
    public static Stage currStage = null;
    //电脑控制的坦克
    public static ArrayList<Tank> computerTanks = new ArrayList<>();
    //子弹
    public static ArrayList<Bullet> bullets = new ArrayList<>();
    //地图块
    public static ArrayList<Block> blocks = new ArrayList<>();
    //爆炸
    public static ArrayList<Explode> explodes = new ArrayList<>();
    //用于暂时存储待删除的对象，防止直接删除引起并发修改异常
    public static ArrayList<Base> objectsToRemove = new ArrayList<>();
    //游戏进行的时间（单位：毫秒）
    public static long playedTime = 0L;

    /**
     * 游戏结果：失败、继续（未结束）、胜利
     */
    public static final int END_FAIL = -1;
    public static final int CONTINUE = 0;
    public static final int END_WIN = 1;
    public static int gameEnd = CONTINUE;

    //从文件中读取关卡信息并初始化
    public static void initStages(){
        stages = new ArrayList<>();
        String dirName = System.getProperty("user.dir")+"\\tankwar\\src\\com\\caesar\\res\\stages";
        File file = new File(dirName);
        String[] list = file.list();
        for (int i = 0; i < list.length; i++) {
            File stageFile = new File(dirName + "\\" + list[i]);
            if(stageFile.isFile()){
                try {
                    stages.add(new Stage(stageFile));
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void judgeGameEnd(){
        //判断游戏失败条件
        if(playerHome.isDestroyed()){
            gameEnd = END_FAIL;
        }
        if(playerCount == 1 && playerOneLife == 0) gameEnd = END_FAIL;
        if(playerCount == 2 && playerOneLife == 0 && playerTwoLife == 0){
            gameEnd = END_FAIL;
        }
        //判断游戏胜利条件
        switch (gameModel){
            case Stage.HOME_MODEL ->{
                if(computerHome.isDestroyed()) gameEnd = END_WIN;
            }
            case Stage.ENEMY_MODEL -> {
                if(computerCount == currStage.getComputerCount() && computerTanks.size() == 0)
                    gameEnd = END_WIN;
            }
            default -> {
                if(playedTime >= currStage.getGameModel() * 1000) gameEnd = END_WIN;
            }
        }
        if(gameEnd != CONTINUE) return;
        gameEnd = CONTINUE;
    }

    public static void clearAll(){
        gameEnd = CONTINUE;
        computerCount = 0;
        playedTime = 0L;
        playerOne = playerTwo = null;
        computerTanks.clear();
        bullets.clear();
        blocks.clear();
        explodes.clear();
        objectsToRemove.clear();
    }

    public static void removeObjects(){
        for (Base base : objectsToRemove) {
            if(base instanceof Tank){
                computerTanks.remove((Tank) base);
            }
            else if(base instanceof Bullet){
                bullets.remove((Bullet) base);
            }
            else if(base instanceof Block){
                blocks.remove((Block) base);
            }
            else if(base instanceof Explode){
                explodes.remove((Explode) base);
            }
            else throw new IllegalStateException("Wrong class to remove in Dao :" + base.getClass());
        }
    }
}
