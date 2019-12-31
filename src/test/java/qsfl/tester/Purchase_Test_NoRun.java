package qsfl.tester;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static qsfl.tester.purchase.Purchase.balances;
import static qsfl.tester.purchase.Purchase.purchaseItem;

public class Purchase_Test_NoRun {
    @Before
    public void popWallet(){
        balances.clear();
        balances.put(1, 100F); //wallet ID 1 has 100 balance
    }

    @Test
    public void test1(){
        int wallet = 1;
        float balanceBefore = balances.get(wallet);
        assertEquals(false,
                purchaseItem(1234, 150, 1, wallet));
        assertTrue(balanceBefore >= balances.get(wallet));
    }

    @Test
    public void test2(){
        int wallet = 1;
        float balanceBefore = balances.get(wallet);
        assertEquals(true,
                purchaseItem(1234, 20, 1, wallet));
        assertTrue(balanceBefore >= balances.get(wallet));
    }

    @Test
    public void test3(){
        int wallet = 1;
        float balanceBefore = balances.get(wallet);
        assertEquals(true,
                purchaseItem(1234, -30, 1, wallet));
        assertTrue(balanceBefore >= balances.get(wallet));
    }
}
