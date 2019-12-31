package qsfl.tester;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static qsfl.tester.leastof.LeastOf.leastOf_bug2;

public class LeastOf3_Bug2_Test_NoRun {
    @Test
    public void test1(){
        assertEquals(1, leastOf_bug2(1,2,3));
    }


    @Test
    public void test2(){
        assertEquals(2, leastOf_bug2(5,2,3));
    }


    @Test
    public void test3(){
        assertEquals(3, leastOf_bug2(5,6,3));
    }


    @Test
    public void test4(){
        assertEquals(1, leastOf_bug2(1,3,2));
    }


    @Test
    public void test5(){
        assertEquals(2, leastOf_bug2(3,2,5));
    }


    @Test
    public void test6(){
        assertEquals(3, leastOf_bug2(6,5,3));
    }

    @Test
    public void test7(){
        assertEquals(-1, leastOf_bug2(-1,5,2));
    }
}
