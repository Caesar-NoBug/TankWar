package com.caesar.bean.common;

public enum Direction{
    //方向：上、下、左、右
    UP, DOWN, LEFT, RIGHT;
    public static Direction valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }

}