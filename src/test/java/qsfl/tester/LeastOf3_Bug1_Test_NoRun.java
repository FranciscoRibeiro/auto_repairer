package qsfl.tester;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static qsfl.tester.leastof.LeastOf.leastOf_bug1;


public class LeastOf3_Bug1_Test_NoRun {

    @Test
    public void test1(){
        assertEquals(1, leastOf_bug1(1,2,3));
    }

    @Test
    public void test2(){
        assertEquals(2, leastOf_bug1(5,2,3));
    }

    @Test
    public void test3(){
        assertEquals(3, leastOf_bug1(5,6,3));
    }

    @Test
    public void test4(){
        assertEquals(1, leastOf_bug1(1,3,2));
    }

    @Test
    public void test5(){
        assertEquals(2, leastOf_bug1(3,2,5));
    }

    @Test
    public void test6(){
        assertEquals(3, leastOf_bug1(6,5,3));
    }

    @Test
    public void test7(){
        assertEquals(4, leastOf_bug1(6,7,4));
    }

    @Test
    public void test8(){
        assertEquals(5, leastOf_bug1(7,8,5));
    }

    @Test
    public void test9(){
        assertEquals(6, leastOf_bug1(8,9,6));
    }

    @Test
    public void test10(){
        assertEquals(7, leastOf_bug1(9,10,7));
    }
    @Test
    public void test11(){
        assertEquals(7, leastOf_bug1(9,10,7));
    }
    @Test
    public void test12(){
        assertEquals(7, leastOf_bug1(9,10,7));
    }
    @Test
    public void test13(){
        assertEquals(7, leastOf_bug1(9,10,7));
    }
    @Test
    public void test14(){
        assertEquals(7, leastOf_bug1(9,10,7));
    }
    @Test
    public void test15(){
        assertEquals(7, leastOf_bug1(9,10,7));
    }
    @Test
    public void test16(){
        assertEquals(7, leastOf_bug1(9,10,7));
    }
}
