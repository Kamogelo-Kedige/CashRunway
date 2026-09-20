package com.CashGuard.analysis;

import com.CashGuard.model.ATM;
import com.CashGuard.model.ATMDayLog;
import com.CashGuard.model.Denomination;

import java.util.List;

/**
 * Works out which notes are the most withdrawn from an ATM and mostly used
 */
public class DenominationAnalyser {

    /**
     * Adds up the total Rand value withdrawn in each denomination across the ATM's entire history.
     */
    public Denomination totalByDenomination(ATM atm) {
        List<ATMDayLog> history = atm.getAtmTransactionHistory();

        if (history == null || history.isEmpty()) {
            throw new IllegalStateException("Cannot analyse denominations with no history for " + atm.getId());
        }

        double totalR10 = 0, totalR20 = 0, totalR50 = 0, totalR100 = 0, totalR200 = 0;

        for (ATMDayLog record : history) {
            Denomination denomination = record.getDenominationBreakdown();

            // safety check so, loop doesn't crash
            if (denomination == null) continue;

            totalR10 += denomination.r10();
            totalR20 += denomination.r20();
            totalR50 += denomination.r50();
            totalR100 += denomination.r100();
            totalR200 += denomination.r200();
        }

        return new Denomination(totalR10, totalR20, totalR50, totalR100, totalR200);
    }

    /**
     * Returns the name of the single most-withdrawn denomination,
     */
    public String mostUsedDenomination(ATM atm) {
        Denomination totals = totalByDenomination(atm);

        double max = totals.r10();
        String label = "R10";

        if (totals.r20() > max)
        {
            max = totals.r20(); label = "R20";
        }
        if (totals.r50() > max)
        {
            max = totals.r50(); label = "R50";
        }
        if (totals.r100() > max)
        {
            max = totals.r100(); label = "R100";
        }
        if (totals.r200() > max)
        {
            max = totals.r200(); label = "R200";
        }

        return label;
    }
}
