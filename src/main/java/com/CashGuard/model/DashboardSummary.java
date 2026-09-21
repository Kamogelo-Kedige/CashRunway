package com.CashGuard.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Bundles everything needed for the dashboard, This is a finished, read-only snapshot
 *
 */
public record DashboardSummary(String atmId,
                               String location,
                               String riskLevel,
                               double hoursToNoCash,
                               double currentBalance,
                               double percentOfCapacity,
                               String recommendedAction,
                               double recommendedLoadAmount,
                               String mostUsedDenomination,
                               Denomination denominationTotals,
                               Map<LocalDate, Double> withdrawalTrend,
                               Map<LocalDate, Double> balanceTrend,
                               Map<LocalDate, Double> depositTrend,
                               double availabilityPercent,
                              List<Warning> recentWarnings)
{ }
