package com.caesar.view;

import com.caesar.bean.common.Bullet;

import com.caesar.bean.common.Direction;
import com.caesar.bean.common.Explode;
import com.caesar.dao.Stage;
import com.caesar.bean.blocks.Block;
import com.caesar.bean.tanks.Tank;
import com.caesar.dao.Dao;
import com.caesar.util.MyUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import static com.caesar.dao.Dao.*;
import static com.caesar.util.Constant.*;

/**
 * @author caesar
 */
public class GameFrame extends JFrame implements Runnable{

    /**
     * 当前运行状态：菜单、游戏、游戏帮助、游戏设置、退出游戏
     */
    private static final int WORK_MENU = 0;
    private static final int WORK_GAME = 1;
    private static final int WORK_HELP = 2;
    private static final int WORK_EXIT = 3;
    private int workState = WORK_MENU;

    /**
     * 菜单状态：开始游戏、继续游戏、游戏帮助、游戏设置、退出游戏
     */
    private static final int MENU_START = 0;
    private static final int MENU_HELP = 1;
    private static final int MENU_EXIT = 2;
    private static int menuState = MENU_START;
    /**
     * 游戏状态：选择地图，选择坦克，进行游戏，游戏暂停，游戏结束
     */
    private static final int GAME_CHOOSE_STAGE = 0;
    private static final int GAME_CHOOSE_TANK = 1;
    private static final int GAME_RUNNING = 2;
    private static final int GAME_PAUSING = 3;
    private static final int GAME_END = 4;
    private static int gameState = GAME_CHOOSE_STAGE;
    //当前关卡编号
    private static int stageIndex = 0;
    //玩家一和玩家二的坦克类型
    private static int playerOneTankType = 0;
    private static int playerTwoTankType = 0;
    //双缓冲图片
    private static BufferedImage bufImg = new BufferedImage(PANE_WIDTH, PANE_HEIGHT,BufferedImage.TYPE_4BYTE_ABGR);
    //用于打印文字信息（获得抗锯齿效果，使文字更平滑）
    private static Graphics2D g2d = bufImg.createGraphics();
    private int helpIndex = 0;

    public GameFrame(){
        initFrame();
        initEventListener();
        new Thread(this).start();
    }

    private void initFrame(){

        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setVisible(true);
        setResizable(false);
        setTitle(GAME_TITLE);

        setIconImage(MyUtil.getImage("com/caesar/res/common/logo.png", FONT_SIZE, FONT_SIZE));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //设置窗口居中显示
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width, screenHeight = screenSize.height;
        setLocation(screenWidth - FRAME_WIDTH >> 1, screenHeight - FRAME_HEIGHT >> 1);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(MENU_FONT);
    }

    private void paintMenuFrame(Graphics g){

        int x = PANE_WIDTH - FONT_SIZE * 5 >> 1;
        int y = PANE_HEIGHT - FONT_SIZE * 23 / 2 >> 1;
        y += FONT_SIZE * 3 / 5;
        for (int i = 0; i < MENUS.length; i++) {
            g.drawImage(BUTTON_BK, x, y, null);
            y += FONT_SIZE * 3;
        }

        x = PANE_WIDTH - FONT_SIZE * 4 >> 1;
        y = PANE_HEIGHT - FONT_SIZE * 8 >> 1;

        g.drawImage(CHOOSE, x - FONT_SIZE * 2, y - FONT_SIZE * 5 / 6 + menuState * FONT_SIZE * 3, null);

        g2d.setColor(Color.WHITE);

        for (int i = 0; i < MENUS.length; i++) {
            g2d.drawString(MENUS[i], x, y);
            y += FONT_SIZE * 3;
        }

    }

    private void paintGameFrame(Graphics g){
        switch (gameState){
            case GAME_CHOOSE_STAGE -> paintGameChooseStageFrame(g);
            case GAME_CHOOSE_TANK -> paintGameChooseTankFrame(g);
            case GAME_RUNNING -> paintGameRunningFrame(g);
            case GAME_PAUSING -> paintGamePausingFrame(g);
            case GAME_END -> paintGameEndFrame(g);
            default -> throw new IllegalStateException("gameState:" + gameState);
        }
    }

    private void paintGameChooseStageFrame(Graphics g){
        if(stages == null) Dao.initStages();
        if(stages.size() == 0) return;

        g.drawImage(CHART_BK, PANE_WIDTH - STAGE_BK_WIDTH >> 1, PANE_HEIGHT - STAGE_BK_HEIGHT >> 1, null);

        g2d.setColor(Color.WHITE);
        g2d.drawString("选择地图", PANE_WIDTH - FONT_SIZE * 4 >> 1,
                PANE_HEIGHT - FONT_SIZE * 2 - Stage.CHART_HEIGHT >> 1);
        stages.get(stageIndex).paint(g);
    }

    private void paintGameChooseTankFrame(Graphics g) {
        if(playerCount == 1){
            g.drawImage(TANKS_IMAGE[playerColor][playerOneTankType][Direction.UP.ordinal()],
                    PANE_WIDTH - TANK_SIZES[playerOneTankType][0] >> 1,
                    PANE_HEIGHT- TANK_SIZES[playerOneTankType][1] >> 1, null);
        }
        else if(playerCount == 2){
            int x = PANE_WIDTH - TANK_SIZES[playerOneTankType][0] * 3 >> 1;
            int y = PANE_HEIGHT- TANK_SIZES[playerOneTankType][1] >> 1;
            g.drawImage(TANKS_IMAGE[playerColor][playerOneTankType][Direction.UP.ordinal()],
                    x, y, null);

            g.drawImage(TANKS_IMAGE[playerColor][playerTwoTankType][Direction.UP.ordinal()],
                    x + TANK_SIZES[playerOneTankType][0] * 2,
                    y, null);
        }
    }

    private void paintGameRunningFrame(Graphics g){

        judgeGameEnd();

        if(gameEnd == END_FAIL || gameEnd == END_WIN){
            gameState = GAME_END; return;
        }

        g.drawImage(STAGE_BK, 0, 0, null);

        currStage.initDynamic();

        if(playerCount >= 1)
        playerOne.paint(g);

        if(playerCount == 2)
        playerTwo.paint(g);

        for (Tank tank : computerTanks) {
            tank.paint(g);
        }

        for (Bullet bullet : bullets){
            bullet.paint(g);
        }

        for(Block block : blocks){
            block.paint(g);
        }

        for(Explode explode : explodes){
            explode.paint(g);
        }

        removeObjects();
    }

    private void paintGamePausingFrame(Graphics g){
        g.drawImage(DIALOG_BK, PANE_WIDTH - DIALOG_BK_WIDTH >> 1,
                PANE_HEIGHT - DIALOG_BK_HEIGHT >> 1, null);
        g2d.setColor(Color.YELLOW);
        g2d.drawString("游戏暂停中",
                PANE_WIDTH - FONT_SIZE * 5 >> 1, PANE_HEIGHT + FONT_SIZE / 2 >> 1);
    }

    private void paintGameEndFrame(Graphics g){
        g.drawImage(DIALOG_BK, PANE_WIDTH - DIALOG_BK_WIDTH >> 1,
                PANE_HEIGHT - DIALOG_BK_HEIGHT >> 1, null);
        Color color = gameEnd == END_WIN ? new Color(0x56C596) : Color.YELLOW;
        g2d.setColor(color);
        String gameEndInfo = gameEnd == END_FAIL ? "游戏失败" : "游戏胜利";

        g2d.drawString(gameEndInfo,
                PANE_WIDTH - FONT_SIZE * 4 >> 1, PANE_HEIGHT + FONT_SIZE / 2 >> 1);
    }

    private void paintHelpFrame(Graphics g){
        g.drawImage(HELP_INFORMATION[helpIndex], 0, 0, null);
    }

    private void initEventListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                switch (workState) {
                    case WORK_MENU -> keyPressedEventMenu(keyCode);
                    case WORK_GAME -> keyPressedEventGame(keyCode);
                    case WORK_HELP -> keyPressedEventHelp(keyCode);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if(workState == WORK_GAME && gameState == GAME_RUNNING)
                    keyReleasedEventGameRunning(keyCode);
            }

        });

    }

    private void keyPressedEventHelp(int keyCode) {

        switch (keyCode){
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_A:
            case KeyEvent.VK_S:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
            case KeyEvent.VK_D:
                helpIndex = helpIndex ^ 1;
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                    workState = WORK_MENU;
                break;
        }
    }

    private void keyReleasedEventGameRunning(int keyCode) {
        switch (keyCode){
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_ENTER:
                if(playerTwo != null && playerTwo.isAlive())
                playerTwo.keyReleasedEvent(keyCode);
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_S:
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
            case KeyEvent.VK_SPACE:
                if(playerOne.isAlive())
                playerOne.keyReleasedEvent(keyCode);
                break;
        }

    }

    private void keyPressedEventGame(int keyCode) {
        //选择关卡
        if(gameState == GAME_CHOOSE_STAGE){
            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_A:
                case KeyEvent.VK_S:
                    if(-- stageIndex < 0) stageIndex = stages.size() - 1;
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                case KeyEvent.VK_D:
                    if(++ stageIndex >= stages.size()) stageIndex = 0;
                    break;
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SPACE:
                    currStage = stages.get(stageIndex);
                    currStage.initStatics();
                    gameState = GAME_CHOOSE_TANK;
                    break;
            }
        }
        //选择坦克
        else if(gameState == GAME_CHOOSE_TANK){
            switch (keyCode) {
                case KeyEvent.VK_LEFT -> {
                    if(--playerTwoTankType < 0) playerTwoTankType = TANK_TYPE_COUNT - 1;
                }
                case KeyEvent.VK_RIGHT -> {
                    if(++playerTwoTankType == TANK_TYPE_COUNT) playerTwoTankType = 0;
                }
                case KeyEvent.VK_A -> {
                    if(--playerOneTankType < 0) playerOneTankType = TANK_TYPE_COUNT - 1;
                }
                case KeyEvent.VK_D -> {
                    if(++playerOneTankType == TANK_TYPE_COUNT) playerOneTankType = 0;
                }
                case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> gameState = GAME_RUNNING;
            }
        }
        //游戏
        else if(gameState == GAME_RUNNING){
            switch (keyCode) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_S:
                case KeyEvent.VK_A:
                case KeyEvent.VK_D:
                    if(playerOne.isAlive())
                        playerOne.keyPressedEvent(keyCode);
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                    if(playerCount == 2 && playerTwo.isAlive())
                    playerTwo.keyPressedEvent(keyCode);
                    break;
                case KeyEvent.VK_P:
                        gameState = GAME_PAUSING;
                    break;
                case KeyEvent.VK_ESCAPE:
                    gameState = GAME_END;
            }
        }
        else if(gameState == GAME_PAUSING)
            {
                gameState = GAME_RUNNING;
        }
        else if(gameState == GAME_END){
            if(keyCode == KeyEvent.VK_ENTER){
                Dao.clearAll();
                gameState = GAME_CHOOSE_STAGE;
                workState = WORK_MENU;
            }
        }
    }

    private void keyPressedEventMenu(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (--menuState < 0) {
                    menuState = MENUS.length - 1;
                }
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if(++menuState > MENUS.length -1){
                    menuState = 0;
                }
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                switch(menuState){
                    case MENU_START -> setWorkState(WORK_GAME);
                    case MENU_HELP  -> setWorkState(WORK_HELP);
                    case MENU_EXIT -> setWorkState(WORK_EXIT);
                    default -> {}
                }
                break;
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics bufG = bufImg.getGraphics();
        bufG.drawImage(ALL_BK, 0, 0, null);

        switch (workState) {
            case WORK_MENU -> paintMenuFrame(bufG);
            case WORK_GAME -> paintGameFrame(bufG);
            case WORK_HELP -> paintHelpFrame(bufG);
            case WORK_EXIT -> System.exit(EXIT_ON_CLOSE);
            default -> throw new IllegalStateException("Unexpected value: " + workState);
        }

        getGraphics().drawImage(bufImg, LEVEL_INSET, UP_INSET, null);
    }


    /**
     * 持续刷新窗口的方法
     */
    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(REPAINT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            repaint();
            playedTime += REPAINT_INTERVAL;
        }
    }

    public static int getPlayerOneTankType() {
        return playerOneTankType;
    }

    public static int getPlayerTwoTankType() {
        return playerTwoTankType;
    }

    public void setWorkState(int workState) {
        this.workState = workState;
    }
}
