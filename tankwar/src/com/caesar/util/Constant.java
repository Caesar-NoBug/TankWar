package com.caesar.util;

import com.caesar.bean.common.Direction;

import java.awt.*;

/**
 * 用于维护游戏中的常量
 * @author caesar
 */
public class Constant {

    public Constant(){

    }
    /**
     * 游戏刷新率:30帧
     */
    public static final long REPAINT_INTERVAL = 1000 / 30;

    /**
     * 窗口尺寸
     */
    public static final int FRAME_WIDTH = 989;
    public static final int FRAME_HEIGHT = 787;

    /**
     *实际显示区域的尺寸
     */
    public static final int PANE_WIDTH = 975;
    public static final int PANE_HEIGHT = 750;
    /**
     * 窗口上边框宽度和左右边框宽度
     */
    public static final int UP_INSET = 30;
    public static final int LEVEL_INSET = 7;

    //字体大小
    public static final int FONT_SIZE = 44;
    //标题
    public static final String GAME_TITLE = "坦克大战";
    //菜单内容
    public static final String[] MENUS = {"开始游戏", "游戏帮助", "退出游戏"};
    //默认字体
    public static final Font MENU_FONT = new Font("楷体", Font.BOLD, FONT_SIZE);
    //图片
    public static final int BUTTON_WIDTH = FONT_SIZE * 5;
    public static final int BUTTON_HEIGHT = FONT_SIZE * 3 / 2;
    public static final Image ALL_BK;
    public static final Image BUTTON_BK;
    public static final Image STAGE_BK;
    public static final Image CHOOSE;
    //坦克颜色种类数
    private static final int COLOR_COUNT = 2;
    //4种坦克类型：A、B、C、D
    public static final int TANK_TYPE_COUNT = 4;
    //坦克尺寸(A、B、C、D)
    public static final int[][] TANK_SIZES = {{50, 64}, {50, 61}, {50, 70}, {50, 70}};
    //4种方向：上、下、左、右
    private static final int DIRECTION_COUNT = 4;
    //各种坦克图片
    private static final String[] tankTypeNames = {"A", "B", "C", "D"};
    public static final Image[][][] TANKS_IMAGE = new Image[COLOR_COUNT][TANK_TYPE_COUNT][DIRECTION_COUNT];
    //子弹图片
    public static final int BULLET_WIDTH = 5;
    public static final int BULLET_HEIGHT = 25;
    public static final Image[] BULLET_IMAGES = new Image[DIRECTION_COUNT];
    //爆炸图片
    public static final int BOOM_FRAME = 9;
    public static final int BOOM_SIZE = 70;
    public static final Image[] BOOM_IMAGES = new Image[BOOM_FRAME];

    //地图块图片
    public static final int BLOCK_TYPE_COUNT = 5;
    public static final int BLOCK_SIZE = 75;
    public static final Image[] BLOCK_IMAGES = new Image[BLOCK_TYPE_COUNT];
    //关卡的背景图片
    public static final int STAGE_BK_WIDTH = 596;
    public static final int STAGE_BK_HEIGHT = 497;
    public static final Image CHART_BK;
    //对话框的背景图片
    public static final int DIALOG_BK_WIDTH = 591;
    public static final int DIALOG_BK_HEIGHT = 133;
    public static final Image DIALOG_BK;

    public static final Image[] HELP_INFORMATION = new Image[2];

    static {
        ALL_BK = MyUtil.getImage("com/caesar/res/BKs/allBK.png", PANE_WIDTH, PANE_HEIGHT);
        BUTTON_BK = MyUtil.getImage("com/caesar/res/BKs/buttonBK.png", BUTTON_WIDTH, BUTTON_HEIGHT);
        STAGE_BK = MyUtil.getImage("com/caesar/res/BKs/stageBK.jpg", PANE_WIDTH, PANE_HEIGHT);
        CHOOSE = MyUtil.getImage("com/caesar/res/common/choose.png", FONT_SIZE , FONT_SIZE );
        CHART_BK = MyUtil.getImage("com/caesar/res/BKs/chartBK.png", STAGE_BK_WIDTH, STAGE_BK_HEIGHT);
        DIALOG_BK = MyUtil.getImage("com/caesar/res/BKs/dialogBK1.png", DIALOG_BK_WIDTH, DIALOG_BK_HEIGHT);
        HELP_INFORMATION[0] = MyUtil.getImage("com/caesar/res/common/helpInfo0.png", PANE_WIDTH, PANE_HEIGHT);
        HELP_INFORMATION[1] = MyUtil.getImage("com/caesar/res/common/helpInfo1.png", PANE_WIDTH, PANE_HEIGHT);
        //导入各种坦克图片
        for (int i = 0; i < COLOR_COUNT; i++) {
            for (int j = 0; j < TANK_TYPE_COUNT; j++) {
                for (int k = 0; k < DIRECTION_COUNT; k++) {
                    TANKS_IMAGE[i][j][k] = MyUtil.getImage("com/caesar/res/tanks/tank_" +
                            (i == 0 ? "b" : "r") + "_" + tankTypeNames[j] + "_" + (k + 1) + ".png"
                            ,MyUtil.getTankDimension(j, Direction.valueOf(k)));
                }
            }
        }

        for (int i = 0; i < DIRECTION_COUNT; i++) {
            Dimension dimension = MyUtil.getBulletDimension(Direction.valueOf(i));
            int width = dimension.width, height = dimension.height;
            BULLET_IMAGES[i] = MyUtil.getImage("com/caesar/res/bullet/bullet_"
                            + (i + 1) + ".png", width, height);
        }

        for (int i = 0; i < BOOM_FRAME; i++) {
            BOOM_IMAGES[i] = MyUtil.getImage("com/caesar/res/boom/boom_"
                    + (i + 1) + ".png", BOOM_SIZE, BOOM_SIZE);
        }

        for (int i = 0; i < BLOCK_TYPE_COUNT; i++) {
            BLOCK_IMAGES[i] = MyUtil.getImage("com/caesar/res/blocks/block_" + i + ".png",
                    BLOCK_SIZE, BLOCK_SIZE);
        }
    }

}