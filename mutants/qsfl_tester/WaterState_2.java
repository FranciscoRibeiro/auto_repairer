package qsfl.tester.waterstate;

public class WaterState {

    public enum WATER_STATE {

        SOLID, LIQUID, GAS
    }

    public static WATER_STATE waterState(float waterTemp) {
        if (waterTemp > 0) {
            return WATER_STATE.SOLID;
        } else if (waterTemp > 100) {
            return WATER_STATE.GAS;
        } else {
            return WATER_STATE.LIQUID;
        }
    }
}
