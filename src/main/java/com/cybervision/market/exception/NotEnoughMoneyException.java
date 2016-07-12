package com.cybervision.market.exception;

public class NotEnoughMoneyException extends Exception {
    public NotEnoughMoneyException() {
        System.err.println("Error: not enough money on credit card.");
    }

    public NotEnoughMoneyException(Double youHave, Double youNeed) {
        System.err.printf("Not enough money for buying. You have: %.2f, but you need: %.2f", youHave, youNeed);
    }

}
