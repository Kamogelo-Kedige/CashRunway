package com.CashGuard.analysis;

import com.CashGuard.model.ATM;
import com.CashGuard.model.ATMDayLog;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks withdrawal trends
 */
public class TrendAnalyser
{
        /**
         * Returns a date to withdrawal amount map, in date order.
         * @param atm
         */
        public Map<LocalDate, Double> dailyWithdrawalSeries(ATM atm) {
            List<ATMDayLog> history = atm.getAtmTransactionHistory();

            if (history == null || history.isEmpty()) {
                throw new IllegalStateException("Cannot build a trend with no history for " + atm.getId());
            }

            //Linked hashmap to preserve order in which the transactions were performed
            //get withdrawal amounts for each date
            Map<LocalDate, Double> withdrawalSeries = new LinkedHashMap<>();
            for (ATMDayLog dayRecord : history) {
                withdrawalSeries.put(dayRecord.getDate(), dayRecord.getWithdrawalAmount());
            }
            return withdrawalSeries;
        }

        /**
         * Returns a date to closing balance amount map, in date order.
         */
        public Map<LocalDate, Double> dailyBalanceSeries(ATM atm) {
            List<ATMDayLog> history = atm.getAtmTransactionHistory();

            if (history == null || history.isEmpty()) {
                throw new IllegalStateException("Cannot build a trend with no history for " + atm.getId());
            }

            //get closing balances for each date
            Map<LocalDate, Double> closingBalanceSeries = new LinkedHashMap<>();
            for (ATMDayLog dayRecord : history) {
                closingBalanceSeries.put(dayRecord.getDate(), dayRecord.getClosingBalance());
            }
            return closingBalanceSeries;
        }


    /**
     *
     * Cash customers deposited back into the ATM each day
     *
     * @param atm
     * @return Returns a date to deposit amount map, in date order
     */
    public Map<LocalDate, Double> dailyDepositSeries(ATM atm) {
        List<ATMDayLog> history = atm.getAtmTransactionHistory();

        if (history == null || history.isEmpty()) {
            throw new IllegalStateException("Cannot build a trend with no history for " + atm.getId());
        }

        Map<LocalDate, Double> depositSeries = new LinkedHashMap<>();
        for (ATMDayLog dayRecord : history) {
            depositSeries.put(dayRecord.getDate(), dayRecord.getDeposits());
        }
        return depositSeries;
    }
}

