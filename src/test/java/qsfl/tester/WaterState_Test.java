package qsfl.tester;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static qsfl.tester.waterstate.WaterState.*;
import static qsfl.tester.waterstate.WaterState.waterState;

public class WaterState_Test {
    /*@Test
    public void test1(){
        assertEquals(WATER_STATE.GAS, waterState(100));
    }*/

    @Test
    public void test2(){
        assertEquals(WATER_STATE.SOLID, waterState(0));
    }

    @Test
    public void test3(){
        assertEquals(WATER_STATE.SOLID, waterState(-20));
    }

    @Test
    public void test4(){
        assertEquals(WATER_STATE.LIQUID, waterState(20));
    }

    @Test
    public void test5(){
        assertEquals(WATER_STATE.GAS, waterState(120));
    }
}
