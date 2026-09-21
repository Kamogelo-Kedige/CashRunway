package com.CashGuard.analysis;

import com.CashGuard.model.ATM;
import com.CashGuard.model.DashboardSummary;
import com.CashGuard.model.Warning;

import java.util.List;

/**
 *  * Runs an ATM's data through the predictor, denomination analyser,
 *  * and trend analyser, and packages the results into one
 *  * DashboardSummary.
 */
public class Dashboard {

    public DashboardSummary build(ATM atm) {
        CashShortagePredictor predictor = new CashShortagePredictor();
        DenominationAnalyser denominationAnalyzer = new DenominationAnalyser();
        TrendAnalyser trendAnalyzer = new TrendAnalyser();



        double currentBalance = atm.getLatestRecord().getClosingBalance();
        double percentOfCapacity = (currentBalance / atm.getMaxCashCapacity()) * 100;
        String riskLevel = predictor.determineRiskLevel(atm);
        List<Warning> warnings = predictor.lastFiveWarnings(atm);

        return new DashboardSummary(
                atm.getId(),
                atm.getLocation(),
                predictor.determineRiskLevel(atm),
                predictor.predictHoursToNoCash(atm),
                currentBalance,
                percentOfCapacity,
                recommendedAction(riskLevel),
                denominationAnalyzer.mostUsedDenomination(atm),
                denominationAnalyzer.totalByDenomination(atm),
                trendAnalyzer.dailyWithdrawalSeries(atm),
                trendAnalyzer.dailyBalanceSeries(atm),
                trendAnalyzer.dailyDepositSeries(atm),
                trendAnalyzer.calculateAvailabilityPercent(atm),
                warnings
        );

    }

    /**
     * Turns a risk level into an actionable instruction for operations staff.
     */
    private String recommendedAction(String riskLevel) {
        return switch (riskLevel) {
            case "Critical" -> "Dispatch emergency CIT now";
            case "High" -> "Schedule refill within 24 hours";
            case "Medium" -> "Include in next scheduled CIT run";
            case "Low" -> "No action needed";
            default -> "Awaiting sufficient data";
        };
    }
}
