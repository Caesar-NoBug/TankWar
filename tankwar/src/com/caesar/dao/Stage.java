package com.caesar.dao;

import com.caesar.bean.Base;
import com.caesar.bean.blocks.Block;
import com.caesar.bean.blocks.Home;
import com.caesar.bean.Computer;
import com.caesar.bean.Player;
import com.caesar.bean.tanks.Tank;
import com.caesar.view.GameFrame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static com.caesar.util.Constant.*;

/**
 * 用于格式化存储所有关卡的关卡信息
 */
public class Stage extends Base {

    /**
     * 地图横纵向能容纳的最大地图块数量
     */
    private Image image = null;
    private static final int MAP_WIDTH = PANE_WIDTH / BLOCK_SIZE;
    private static final int MAP_HEIGHT = PANE_HEIGHT / BLOCK_SIZE;
    private int playerColor;

    public static final int CHART_WIDTH = 487;
    public static final int CHART_HEIGHT = 375;

    private ArrayList<BlockParam> blockParams = new ArrayList<>();
    private ArrayList<TankParam> tankParams = new ArrayList<>();
    private ArrayList<TankParam> tankParamsRecord = new ArrayList<>();
    private TankParam playerOneParam;
    private TankParam playerTwoParam;

    private int computerCount = 0;
    //游戏模式：（注：以下指胜利条件，游戏失败条件为玩家坦克被全歼或基地被摧毁）
    //家园模式：摧毁敌方基地即为胜利
    public static final int HOME_MODEL = -1;
    //歼灭模式：全歼敌方坦克为胜利
    public static final int ENEMY_MODEL = 0;
    //默认为家园模式，此外任何正数都表示守卫模式（坚持t秒即达到游戏胜利）
    private int gameModel = HOME_MODEL;

    private long gameTime = 0L;
    //默认每2秒生成一个敌方坦克
    private static final long DEFAULT_GENERATE_INTERVAL = REPAINT_INTERVAL * 2;
    private long generateInterval = DEFAULT_GENERATE_INTERVAL;

    private int playerLife = DEFAULT_LIFE;
    //默认玩家有三条命
    private static final int DEFAULT_LIFE = 3;
    private int playerCount = 0;
    //判断是否为重新初始化
    private boolean isReinit = false;

    /**
     * @param file 待读取的文件
     * 文件格式为：
     * 前 10 行为地图中地图块的位置和类型(这里对坐标进行了简化处理，坐标单位为墙的宽度)
     * 各地图块表示:
     *  : 空白
     * 1: 普通的墙
     * 2: 坚固的墙
     * 3: 无法击毁的墙
     * 4: 草丛
     * 5: 电脑基地
     * 6: 玩家基地
     * 第10行以后为各种游戏配置信息，格式为：（不限顺序，但必须以end结尾）
     * 坦克型号:(生成横坐标,生成纵坐标)*生成数量
     * 玩家信息为：P1:(生成横坐标,生成纵坐标)和P2:(生成横坐标,生成纵坐标)
     * 玩家生命次数：life:n
     * 坦克生成间隔为（非必选项）：interval:t
     * 胜利条件：
     * home -摧毁敌方基地
     * enemy -全歼敌方坦克
     * time:t -坚持 t 秒
     * 最后以end结束
     * 示例：
     * 3    252    3
     * 3    222    3
     * 3     3     3
     * 3     3     3
     * 3     1     3
     * 3     1     3
     * 3     3     3
     * 3     3     3
     * 3    222    3
     * 3    262    3
     * A:(1,1)*1,(10,1)*1
     * C:(6,5)*5
     * P1:(0,9)
     * life:1
     * interval:0
     * time:180
     * end
     */
    public Stage(File file) throws IOException {
        BufferedReader bufferedReader = null;
        //随机设置玩家的坦克颜色
        playerColor =  (System.currentTimeMillis() & 1) == 0 ? Tank.BLUE : Tank.RED;

        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line = null;
            //读取地图信息
            for (int i = 0; i < MAP_HEIGHT; i++) {
                line = bufferedReader.readLine();
                for (int j = 0; j < MAP_WIDTH; j++) {
                    //如果为空格则跳过
                    if(line.charAt(j) == ' ') continue;
                    int blockType = line.charAt(j) - '0';
                    //若该区域为非法值一律跳过
                    if(blockType <= 0 || blockType > 6) continue;
                    else blockParams.add(new BlockParam(j, i, blockType));
                }
            }
            //读取坦克信息和游戏模式
            while ((line = bufferedReader.readLine()) != null){
                char ch = line.charAt(0);
                int index = 2;
                //敌方坦克信息
                if(ch >= 'A' && ch <= 'D'){
                    String s = line.replaceAll("\\*", "");
                    StringTokenizer st = new StringTokenizer(s, "\\(|\\)|,");
                    st.nextToken();
                    while(st.hasMoreTokens()){
                        int x = Integer.parseInt(st.nextToken());
                        int y = Integer.parseInt(st.nextToken());
                        int count = Integer.parseInt(st.nextToken());
                        tankParams.add(new TankParam(x, y, playerColor ^ 1, ch - 'A', count));
                        computerCount ++;
                    }
                }
                //玩家信息
                else if(ch == 'P'){
                    StringTokenizer st = new StringTokenizer(line, "\\(|\\)|,");
                    int playerIndex = st.nextToken().charAt(1);
                    //玩家坦克类型由玩家指定
                    TankParam tankParams = new TankParam(Integer.parseInt(st.nextToken()),
                            Integer.parseInt(st.nextToken()), playerColor, -1 , 1);
                    if(playerIndex == '1'){
                        playerOneParam = tankParams; playerCount ++;
                    }
                    else if(playerIndex == '2'){
                        playerTwoParam = tankParams; playerCount ++;
                    }
                }
                else if(line.equals("home")){
                    gameModel = HOME_MODEL;
                }else if(line.equals("enemy")){
                    gameModel = ENEMY_MODEL;
                }else if(line.startsWith("time")){
                    gameModel = Integer.parseInt(line.replaceAll("time:", ""));
                }else if(line.startsWith("interval")){
                    generateInterval = REPAINT_INTERVAL *
                            Integer.parseInt(line.replaceAll("interval:", ""));
                }else if(line.startsWith("life")){
                    playerLife = Integer.parseInt(line.replaceAll("life:", ""));
                }
                else if(line.equals("end")){
                    break;
                }
            }
            for (TankParam tankParam : tankParams) {
                tankParamsRecord.add(tankParam.clone());
            }
        } catch (FileNotFoundException | ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
            throw new RuntimeException("文件格式错误");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } finally {
            if(bufferedReader != null) bufferedReader.close();
        }
    }

    public int getGameModel() {
        return gameModel;
    }

    /**
     * @param g
     * 用于绘制一张略缩图，给用户进行关卡选择
     */
    @Override
    public void paint(Graphics g) {
        if(image == null) initChart();
        //图片居中显示
        g.drawImage(image, PANE_WIDTH - CHART_WIDTH >> 1, PANE_HEIGHT - CHART_HEIGHT >> 1, null);
    }

    private void initChart() {

        image = new BufferedImage(PANE_WIDTH, PANE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g1 = image.getGraphics();

        g1.drawImage(STAGE_BK, 0, 0, null);
        for (BlockParam blockParam : blockParams) {
            if(blockParam.blockType < 5){
                Block.createBlock(blockParam.x, blockParam.y, blockParam.blockType - 1).paint(g1);
            }
            else new Home(blockParam.x, blockParam.y, blockParam.blockType == 5).paint(g1);
        }

        image = image.getScaledInstance(CHART_WIDTH, CHART_HEIGHT, Image.SCALE_DEFAULT);
    }

    /**
     * 用于加载静态信息
     */
    public void initStatics(){
        if(isReinit)
        for (TankParam tankParam : tankParamsRecord) {
            try {
                tankParams.add(tankParam.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        for (BlockParam blockParam : blockParams) {
            if(blockParam.blockType < 5){
                Dao.blocks.add(Block.createBlock(blockParam.x, blockParam.y, blockParam.blockType - 1));
            }
            else{
                boolean isComputer = blockParam.blockType == 5;
                Home home = new Home(blockParam.x, blockParam.y, isComputer);
                Dao.blocks.add(home);
                if(isComputer) Dao.computerHome = home;
                else Dao.playerHome = home;
            }
        }
        Dao.gameModel = gameModel;
        Dao.playerCount = playerCount;
        Dao.playerColor = playerColor;
        Dao.playerOneLife = playerLife;
        if(playerCount == 2) Dao.playerTwoLife = playerLife;
    }

    /**
     * 用于加载动态信息
     */
    public void initDynamic(){
        isReinit = true;
        gameTime ++;
        if(Dao.playerOne == null || Dao.playerOne.isReborn()) {
            int playerOneTankType = GameFrame.getPlayerOneTankType();
            Dao.playerOne = new Player(playerOneParam.x, playerOneParam.y,
                    playerColor, playerOneTankType, 1);
        }
        if((playerCount == 2 && Dao.playerTwo == null) || (Dao.playerTwo != null && Dao.playerTwo.isReborn())) {
            int playerTwoTankType = GameFrame.getPlayerTwoTankType();
            Dao.playerTwo = new Player(playerTwoParam.x, playerTwoParam.y,
                    playerColor, playerTwoTankType, 2);
        }
        //为避免除以0，间隔为0时特殊处理:直接生成全部坦克
        if(generateInterval == 0){
            if(tankParams.size() > 0) {
                for (TankParam tankParam : tankParams) {
                    for (int i = 0; i < tankParam.tankCount; i++) {
                        Dao.computerTanks.add(new Computer(tankParam.x, tankParam.y,
                                tankParam.tankColor, tankParam.tankType).getTank());
                        Dao.computerCount ++;
                    }
                }
                tankParams.clear();
            }
            return;
        }

        if(gameTime % generateInterval == 0){
            if(tankParams.size() == 0) return;
            //防止溢出
            gameTime = 0;
            //随机生成一辆队列中的坦克
            int index = (int) Math.random() * tankParams.size();
            TankParam tankParam = tankParams.get(index);
            if(-- tankParam.tankCount < 0){
                tankParams.remove(index);
                return;
            }
            Dao.computerTanks.add(new Computer(tankParam.x, tankParam.y, tankParam.tankColor, tankParam.tankType).getTank());
            Dao.computerCount ++;
        }
    }

    public int getComputerCount() {
        return computerCount;
    }

    public static class BlockParam {
        public int x;
        public int y;
        public int blockType;

        public BlockParam(int x, int y, int blockType) {
            this.x = x * BLOCK_SIZE;
            this.y = y * BLOCK_SIZE;
            this.blockType = blockType;
        }

        @Override
        public String toString() {
            return "BlockParam{" +
                    "x=" + x +
                    ", y=" + y +
                    ", blockType=" + blockType +
                    '}';
        }
    }

    public static class TankParam implements Cloneable{
        public int x;
        public int y;
        public int tankColor;
        public int tankType;
        public int tankCount;

        public TankParam(int x, int y,int tankColor, int tankType, int tankCount) {
            this.x = x * BLOCK_SIZE;
            this.y = y * BLOCK_SIZE;
            this.tankColor = tankColor;
            this.tankType = tankType;
            this.tankCount = tankCount;
        }

        @Override
        public String toString() {
            return "TankParam{" +
                    "x=" + x +
                    ", y=" + y +
                    ", tankColor=" + tankColor +
                    ", tankType=" + tankType +
                    ", tankCount=" + tankCount +
                    '}';
        }

        @Override
        public TankParam clone() throws CloneNotSupportedException {
            return (TankParam) super.clone();
        }
    }
}
