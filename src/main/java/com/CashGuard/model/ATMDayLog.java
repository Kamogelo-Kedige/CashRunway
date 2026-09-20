package com.CashGuard.model;

import java.time.LocalDate;

/**
 * Represents a single day's worth of transaction activities for a specific ATM
 */
public class ATMDayLog
{
    //Private Attributes
    private LocalDate date;
    private double withdrawalAmount;
    private double deposits;
    private double closingBalance;
    private boolean downtime; // gotta use this
    private boolean isPayday;
    private boolean isMonthEnd;
    private boolean isPublicHoliday;
    private Denomination denominationBreakdown;

    /**
     *No args constructor
     */
    public ATMDayLog()
    {}

    /**
     * Parameterized Constructor
     * @param date
     * @param withdrawalAmount
     * @param deposits
     * @param downtime
     * @param closingBalance
     * @param isPayday
     * @param isMonthEnd
     * @param isPublicHoliday
     * @param denominationBreakdown
     */
    public ATMDayLog(LocalDate date, double withdrawalAmount, double deposits, boolean downtime, double closingBalance, boolean isPayday, boolean isMonthEnd, boolean isPublicHoliday, Denomination denominationBreakdown)
    {
        this.date = date;
        this.withdrawalAmount = withdrawalAmount;
        this.deposits = deposits;
        this.downtime = downtime;
        this.closingBalance = closingBalance;
        this.isPayday = isPayday;
        this.isMonthEnd = isMonthEnd;
        this.isPublicHoliday = isPublicHoliday;
        this.denominationBreakdown = denominationBreakdown;
    }

    //Getter and Setter Pairs
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(double withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    public double getDeposits() {
        return deposits;
    } // gotta use this insert a deposit line,//gotta have the first deposit


    public void setDeposits(double deposits) {
        this.deposits = deposits;
    }

    public double getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(double closingBalance) {
        this.closingBalance = closingBalance;
    }

    public boolean isDowntime() {
        return downtime;
    }

    public void setDowntime(boolean downtime) {
        this.downtime = downtime;
    }

    public boolean isPublicHoliday() {
        return isPublicHoliday;
    }

    public void setPublicHoliday(boolean publicHoliday) {
        isPublicHoliday = publicHoliday;
    }

    public boolean isPayday() {
        return isPayday;
    }

    public void setPayday(boolean payday) {
        isPayday = payday;
    }

    public boolean isMonthEnd() {
        return isMonthEnd;
    }

    public void setMonthEnd(boolean monthEnd) {
        isMonthEnd = monthEnd;
    }

    public Denomination getDenominationBreakdown() {
        return denominationBreakdown;
    }

    public void setDenominationBreakdown(Denomination denominationBreakdown) {
        this.denominationBreakdown = denominationBreakdown;
    }
}
