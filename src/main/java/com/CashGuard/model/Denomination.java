package com.CashGuard.model;


/**
 * A bundle of the five note denominations withdrawn on a specific day
 * @param r10
 * @param r20
 * @param r50
 * @param r100
 * @param r200
 */
public record Denomination(double r10, double r20, double r50, double r100, double r200)
{
    /**
     * Adds up all five notes to get the total amount withdrawn
     * that day.
     */
    public double total()
    {
        return r10 + r20 + r50 + r100 + r200;
    }
}
