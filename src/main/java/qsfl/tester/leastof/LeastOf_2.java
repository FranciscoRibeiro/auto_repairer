package qsfl.tester.leastof;

public class LeastOf_2 {

    public static int leastOf_bug1(int a, int b, int c) {
        // should be AND
        if (a < b || a < c) {
            /*res = a;*/
            return a;
        } else if (b < c) {
            /*res = b;*/
            return b;
        } else {
            /*res = c;*/
            return c;
        }
    /*return res;*/
    }

    public static int leastOf_bug2(int a, int b, int c) {
        if (a < b && a < c) {
            /*res = a;*/
            return a;
        } else // should be LT
        if (b < c) {
            /*res = b;*/
            return b;
        } else {
            /*res = c;*/
            return c;
        }
    /*return res;*/
    }
}
