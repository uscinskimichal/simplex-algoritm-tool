package util;

import java.math.BigDecimal;

public class Configuration {


    private final int HEIGHT = 600;
    private final int WIDTH = 600;
    public static BigDecimal M = new BigDecimal("1000000");

    public int getSceneWidth() {
        return this.WIDTH;
    }

    public int getSceneHeight(){
        return this.HEIGHT;
    }

    public static void setM(BigDecimal m) {
        M = m;
    }
}
