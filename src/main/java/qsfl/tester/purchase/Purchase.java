package qsfl.tester.purchase;

import java.util.HashMap;

public class Purchase {
    public static HashMap<Integer, Float> balances = new HashMap<>();

    public static boolean purchaseItem(int itemId, float price, int qt, int wallet){
        float total = price * qttDiscount(itemId, qt);
        float balance = balances.get(wallet);
        if(balance - total >= 0){
            balance = balance - total;
            updateBalance(wallet, balance);
            updateStockQuantity(itemId, qt);
            return true;
        }
        return false;
    }

    private static void updateStockQuantity(int itemId, int qt) {
        return;
    }

    private static void updateBalance(int wallet, float balance) {
        balances.put(wallet, balance);
    }

    private static float qttDiscount(int itemId, int qt) {
        return 0.9F;
    }
}
