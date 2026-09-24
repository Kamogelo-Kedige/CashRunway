package com.CashGuard.analysis;

import com.CashGuard.model.ATM;
import com.CashGuard.model.ATMDayLog;
import com.CashGuard.model.Warning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Predicts how soon an ATM is likely to run out of money based on its recent 3-day behaviour
 * Then assigns a risk level
 */
public class CashShortagePredictor
{

    // Number of recent days used to calculate average withdrawals
    private static final int WINDOW_DAYS = 3;

    // If cash falls below 15% of capacity, the ATM has Critical cash levels
    private static final double SAFETY_PERCENTAGE = 15.0;

    //Percentage thresholds to fire a cash running out warning
    private static final double[] WARNING_PERCENTAGES = {50.0, 25.0, 15.0};


    /**
     * Calculates the average daily withdrawal amount using the most recent 3 days.
     * @param atm
     * @return
     */
    public double calculateRecentAverage(ATM atm)
    {
        List<ATMDayLog> atmHistory = atm.getAtmTransactionHistory();
        int historySize = atmHistory.size();

        //Get the starting index for the latest 3-day window
        int startTransactionIndex = Math.max(0, historySize - WINDOW_DAYS);

        double total = 0;
        int count = 0;
        double average = 0;

        for(int i = startTransactionIndex; i < historySize; i++)
        {
            //get the withdrawal amount
            total += atmHistory.get(i).getWithdrawalAmount();
            count++;
        }

        average = total / count;

        return average;
    }

    public double calculateWithdrawalDemandMultiplier(ATM atm, ATMDayLog day)
    {
        List<ATMDayLog> atmHistory = atm.getAtmTransactionHistory();

        //normal typical days
        double normalDaysTotal = 0;
        int normalDaysCount = 0;

        //Special days including payday, holidays or month end
        double specialDaysTotal = 0;
        int specialDaysCount = 0;

        for(ATMDayLog atmDayLog : atmHistory)
        {
            boolean isSpecialDay = atmDayLog.isPayday() ||  atmDayLog.isMonthEnd() || atmDayLog.isPublicHoliday();
            if(isSpecialDay)
            {
                //Handle special days
                specialDaysTotal += atmDayLog.getWithdrawalAmount();
                specialDaysCount++;
            }else
            {
                //Handle normal days
                normalDaysTotal += atmDayLog.getWithdrawalAmount();
                normalDaysCount++;
            }
        }

        boolean todayIsSpecial = day.isPayday() || day.isMonthEnd() || day.isPublicHoliday();

        // If today is normal, or we don't have enough data no adjustment is needed.
        if (!todayIsSpecial || normalDaysCount == 0  || specialDaysCount == 0)
            return 1.0;

        double normalDaysAverage =  normalDaysTotal / normalDaysCount;

        double specialDaysAverage =  specialDaysTotal / specialDaysCount;

        return specialDaysAverage / normalDaysAverage;
    }

    /**
     * Calculates the predicted amount that will likely be withdrawn from the ATM per day.
     * @param atm
     */
    public double calculatePredictedDailyWithdrawal(ATM atm)
    {

        ATMDayLog today = atm.getLatestRecord();
        if (today == null)
        {
            throw new IllegalStateException("Cannot predict with no history for " + atm.getId());
        }
        double recentAverage = calculateRecentAverage(atm);

        double demandMultiplier = calculateWithdrawalDemandMultiplier(atm, today);

        return recentAverage * demandMultiplier;
    }

    /**
     * Predicts how many hours the ATM has likely before its cash balance reaches zero.
     * @param atm
     */
    public double predictHoursToNoCash(ATM atm)
    {

        ATMDayLog today = atm.getLatestRecord();

        double predictedDailyWithdrawal = calculatePredictedDailyWithdrawal(atm);

        //  If no predicted withdrawals then ATM is not expected to run out of cash.
        if (predictedDailyWithdrawal <= 0) {
            return Double.MAX_VALUE;
        }

        double currentBalance = today.getClosingBalance();

        return (currentBalance / predictedDailyWithdrawal) * 24;
    }


    /**
     * Determines a risk level based on the predicted time until the ATM runs out of cash.
     */
    public String determineRiskLevel(ATM atm)
    {

        double hoursToNoCash = predictHoursToNoCash(atm);

        double currentBalance = atm.getLatestRecord().getClosingBalance();

        double percentOfCapacity = (currentBalance / atm.getMaxCashCapacity()) * 100;

        String riskLevel = "";

        if (hoursToNoCash < 24) {

            riskLevel = "Critical";

        } else if (hoursToNoCash < 48) {

            riskLevel = "High";

        } else if (hoursToNoCash < 72) {

            riskLevel = "Medium";

        } else
        {

            riskLevel = "Low";
        }

        // 15% Safety-percentage rule overrides the calculated risk
        if (percentOfCapacity < SAFETY_PERCENTAGE)
        {

            riskLevel = "Critical";
        }

        return riskLevel;
    }

    /**
     * Fires an early warning the first time an ATM's balance drops below each
     *  50%,25%, and 15% capacity
     *
     */
    public List<Warning> buildCapacityWarnings(ATM atm)
    {
        List<Warning> warnings = new ArrayList<>();
        boolean[] fired = new boolean[WARNING_PERCENTAGES.length]; // only three warning thresholds

        for (ATMDayLog log : atm.getAtmTransactionHistory())
        {
            double percentOfCapacity = (log.getClosingBalance
                    () / atm.getMaxCashCapacity()) * 100;

            for (int i = 0; i < WARNING_PERCENTAGES.length; i++)
            {
                if (!fired[i] && percentOfCapacity < WARNING_PERCENTAGES[i])
                {
                    warnings.add(new Warning(log.getDate(),
                            "Balance dropped below " + (int) WARNING_PERCENTAGES[i] + "% of capacity"));
                    fired[i] = true;
                }
                if (fired[i] && percentOfCapacity >= WARNING_PERCENTAGES[i])
                {
                    fired[i] = false;
                }
            }
        }
        return warnings;
    }

    /**
     * Returns the 5 most recent capacity warnings, newest first — this
     * is what the dashboard's warnings panel displays.
     */
    public List<Warning> lastFiveWarnings(ATM atm)
    {
        List<Warning> warnings = buildCapacityWarnings(atm);
        Collections.reverse(warnings);
        return warnings.stream().limit(5).collect(Collectors.toList());
    }

}
