package com.awen.image.photopick.util;

public class CalculateUtil {

    public static float divisor(float a, float b) {
        float temp;
        if (a < b) {
            temp = a;
            a = b;
            b = temp;
        }
        while (b != 0) {
            temp = a % b;
            a = b;
            b = temp;
        }
        return a;
    }

    //求最小公倍数
    public static float multiple(float a, float b) {
        float temp = divisor(a, b);
        return a * b / temp;
    }

    /**
     * 通分
     * @param fz1
     * @param fm1
     * @param fz2
     * @param fm2
     * @return
     */
    public static float tongFen(float fz1, float fm1, float fz2, float fm2) {
        char flag = ' ';
        float gbs = multiple(fm1, fm2);
        //获得通分后新的分子
        float xfz1 = gbs * fz1 / fm1;
        float xfz2 = gbs * fz2 / fm2;

//        Log.e("CalculateUtil", fz1 + "/" + fm1 + "=" + xfz1 + "/" + gbs);
//        Log.e("CalculateUtil", fz2 + "/" + fm2 + "=" + xfz2 + "/" + gbs);
//
//        //通分后分母相同比较分子
//        if (xfz1 < xfz2) {
//            flag = '<';
//        } else if (xfz1 > xfz2) {
//            flag = '>';
//        } else {
//            flag = '=';
//        }
//        return flag;
        return xfz1 / xfz2;
    }
}
