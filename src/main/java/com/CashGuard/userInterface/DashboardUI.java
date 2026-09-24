package com.CashGuard.userInterface;

import com.CashGuard.model.DashboardSummary;
import com.CashGuard.model.Denomination;
import com.CashGuard.model.Warning;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds the ATM dashboard screen.
 * Every method here only ever reads from a DashboardSummary
 */
public class DashboardUI
{

    private static Map<String, DashboardSummary> summariesByAtm;
    private static VBox contentRoot; // the swappable middle section

    public static Scene buildMainScene(Map<String, DashboardSummary> summaries) {
        summariesByAtm = summaries;

        ComboBox<String> atmSelector = new ComboBox<>();
        atmSelector.getItems().addAll(summaries.keySet());
        atmSelector.setValue(summaries.keySet().iterator().next());
        atmSelector.setOnAction(e -> refreshContent(atmSelector.getValue()));

        contentRoot = new VBox(12);
        refreshContent(atmSelector.getValue());

        HBox header = buildHeader(atmSelector);

        VBox root = new VBox(12, header, contentRoot);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("dashboard-root");

        Scene scene = new Scene(root, 900, 700);
        scene.getStylesheets().add(Objects.requireNonNull(DashboardUI.class.getResource("/style.css")).toExternalForm());
        return scene;
    }

    /**
     * Rebuilds only the middle section when the ATM selector changes,
     * instead of rebuilding the whole window.
     */
    private static void refreshContent(String atmName) {
        DashboardSummary summary = summariesByAtm.get(atmName);

        HBox riskRow = buildRiskRow(summary);
        HBox chartsRow = new HBox(12, buildTrendChartPanel(summary), buildDenominationPanel(summary));
        VBox warningsPanel = buildWarningsPanel(summary);

        contentRoot.getChildren().setAll(riskRow, chartsRow, warningsPanel);
    }

    private static HBox buildHeader(ComboBox<String> atmSelector) {
        Label title = new Label("CashRunway");
        title.getStyleClass().add("app-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, title, spacer, atmSelector);
        header.getStyleClass().add("header-bar");
        return header;
    }

    private static HBox buildRiskRow(DashboardSummary summary) {
        VBox riskCard = buildRiskCard(summary);

        HBox kpiCards = new HBox(12,
                buildKpiCard("Current Balance", "R" + String.format("%,.0f", summary.currentBalance())),
                buildKpiCard("Capacity Remaining %", String.format("%.1f%%", summary.percentOfCapacity())),
                buildAvailabilityCard(summary),
                buildActionCard(summary)
        );
        HBox.setHgrow(kpiCards, Priority.ALWAYS);

        return new HBox(12, riskCard, kpiCards);
    }

    private static VBox buildActionCard(DashboardSummary summary) {
        Label label = new Label("Recommended action");
        label.getStyleClass().add("kpi-label");

        Label action = new Label(summary.recommendedAction());
        action.getStyleClass().addAll("kpi-value", "action-" + summary.riskLevel().toLowerCase().replace(" ", "-"));

        Label loadAmount = new Label("Load: R" + String.format("%,.0f", summary.recommendedLoadAmount()));
        loadAmount.getStyleClass().add("kpi-sub");

        VBox card = new VBox(4, label, action, loadAmount);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }



    private static VBox buildRiskCard(DashboardSummary summary) {
        Label label = new Label("Risk Level");
        label.getStyleClass().add("kpi-label");

        Label value = new Label(summary.riskLevel().toUpperCase());
        value.getStyleClass().addAll("risk-badge", "risk-" + summary.riskLevel().toLowerCase().replace(" ", "-"));

String hoursText = summary.hoursToNoCash() < 0 || summary.hoursToNoCash() == Double.MAX_VALUE
                ? "Hours To Empty: N/A"
                : summary.riskLevel().equalsIgnoreCase("Critical")
                  ? "hours to empty: < 24h"
                  : "hours to empty: " + Math.round(summary.hoursToNoCash());


        Label hours = new Label(hoursText);
        hours.getStyleClass().addAll("hours-to-empty", "action-" + summary.riskLevel().toLowerCase().replace(" ", "-"));

        VBox card = new VBox(6, label, value, hours);
        card.getStyleClass().add("card");
        return card;
    }

    private static VBox buildKpiCard(String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("kpi-label");

        Label v = new Label(value);
        v.getStyleClass().add("kpi-value");

        VBox card = new VBox(4, l, v);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private static VBox buildTrendChartPanel(DashboardSummary summary) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Day of month");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(1);
        xAxis.setUpperBound(30);
        xAxis.setTickUnit(5);
        xAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
            @Override public String toString(Number value) { return String.valueOf(value.intValue()); }
            @Override public Number fromString(String s) { return Integer.parseInt(s); }
        });

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (R)");

        AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("30-day balance & withdrawal trend");
        chart.setLegendVisible(true);
        chart.setCreateSymbols(false);
        chart.getStyleClass().add("trend-chart");

        XYChart.Series<Number, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Closing balance");
        for (Map.Entry<LocalDate, Double> entry : summary.balanceTrend().entrySet()) {
            balanceSeries.getData().add(new XYChart.Data<>(entry.getKey().getDayOfMonth(), entry.getValue()));
        }

        // Balance only ever trends downward between refills, so payday
        // activity only shows up there as a steeper slope. Plotting the
        // withdrawal amount itself is what actually shows an upward spike
        // on payday/month-end/holiday dates — the thing we want visible.
        XYChart.Series<Number, Number> withdrawalSeries = new XYChart.Series<>();
        withdrawalSeries.setName("Daily withdrawals");
        for (Map.Entry<LocalDate, Double> entry : summary.withdrawalTrend().entrySet()) {
            withdrawalSeries.getData().add(new XYChart.Data<>(entry.getKey().getDayOfMonth(), entry.getValue()));
        }

        chart.getData().addAll(balanceSeries, withdrawalSeries);

        //The day the ATM received
        Map.Entry<LocalDate, Double> peakDeposit = summary.depositTrend().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        VBox panel = new VBox(6, chart);
        panel.getStyleClass().add("card");
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private static VBox buildDenominationPanel(DashboardSummary summary) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Denomination");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total amount dispensed (R)");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Denomination breakdown");
        chart.setLegendVisible(false);

        Denomination d = summary.denominationTotals();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("R10", d.r10()));
        series.getData().add(new XYChart.Data<>("R20", d.r20()));
        series.getData().add(new XYChart.Data<>("R50", d.r50()));
        series.getData().add(new XYChart.Data<>("R100", d.r100()));
        series.getData().add(new XYChart.Data<>("R200", d.r200()));
        chart.getData().add(series);



        VBox panel = new VBox(6, chart);
        panel.getStyleClass().add("card");
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private static VBox buildWarningsPanel(DashboardSummary summary) {
        Label header = new Label("Early Warnings");
        header.getStyleClass().add("section-heading");

        VBox panel = new VBox(8, header);
        panel.getStyleClass().add("card");

        List<Warning> warnings = summary.recentWarnings();

        if (warnings == null || warnings.isEmpty()) {
            Label placeholder = new Label("No warnings to display.");
            placeholder.getStyleClass().add("warning-entry");
            panel.getChildren().add(placeholder);
            return panel;
        }

        VBox list = new VBox(8);
        for (Warning snap : warnings) {
            Label entry = new Label(snap.date() + ": " + snap.message());
            entry.getStyleClass().addAll("warning-entry", "warning-" + severityOf(snap.message()));
            list.getChildren().add(entry);
        }
        panel.getChildren().add(list);
        return panel;
    }

    private static String severityOf(String message) {
        if (message.contains("Critical") || message.contains("15%")) return "critical";
        if (message.contains("High") || message.contains("25%")) return "high";
        if (message.contains("Medium") || message.contains("50%")) return "medium";
        return "neutral";
    }

    /**
     * Shows the ATM's uptime percentage, derived from the Downtime field
     * in the source data. Directly answers the case study's "keep ATM
     * availability above 99%" goal — colour-coded so a panel can see at
     * a glance whether an ATM is meeting that target.
     */
    private static VBox buildAvailabilityCard(DashboardSummary summary) {
        Label label = new Label("ATM Availability");
        label.getStyleClass().add("kpi-label");

        Label value = new Label(String.format("%.1f%%", summary.availabilityPercent()));
        String styleClass = summary.availabilityPercent() >= 99.0 ? "action-low" : "action-critical";
        value.getStyleClass().addAll("kpi-value", styleClass);

        VBox card = new VBox(4, label, value);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

}
