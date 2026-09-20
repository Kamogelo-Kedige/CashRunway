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
        Label title = new Label("CashRunway dashboard");
        title.getStyleClass().add("app-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, title, spacer, atmSelector);
        header.getStyleClass().add("header-bar");
        return header;
    }

    private static HBox buildRiskRow(DashboardSummary summary) {
        VBox riskCard = buildRiskCard(summary);
        Label action = new Label(summary.recommendedAction());
        action.getStyleClass().addAll("kpi-value", "action-" + summary.riskLevel().toLowerCase().replace(" ", "-"));

        HBox kpiCards = new HBox(12,
                buildKpiCard("Current balance", "R" + String.format("%,.0f", summary.currentBalance())),
                buildKpiCard("% of capacity", String.format("%.1f%%", summary.percentOfCapacity())),
                buildActionCard(summary)
        );
        HBox.setHgrow(kpiCards, Priority.ALWAYS);

        return new HBox(12, riskCard, kpiCards);
    }

    private static VBox buildActionCard(DashboardSummary summary) {
        Label label = new Label("Recommended action");
        label.getStyleClass().add("kpi-label");

        // this is the line you were asking about — it replaces the plain
        // "kpi-value" label that buildKpiCard would have created
        Label action = new Label(summary.recommendedAction());
        action.getStyleClass().addAll("kpi-value", "action-" + summary.riskLevel().toLowerCase().replace(" ", "-"));

        VBox card = new VBox(4, label, action);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }



    private static VBox buildRiskCard(DashboardSummary summary) {
        Label label = new Label("Risk level");
        label.getStyleClass().add("kpi-label");

        Label value = new Label(summary.riskLevel().toUpperCase());
        value.getStyleClass().addAll("risk-badge", "risk-" + summary.riskLevel().toLowerCase().replace(" ", "-"));

        String hoursText = summary.hoursToNoCash() < 0
                ? "hours to empty: N/A"
                : "hours to empty: " + Math.round(summary.hoursToNoCash()) + "h";

        // this is the second line you were asking about — replaces the
        // old plain "kpi-sub"-styled hours label
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
        yAxis.setLabel("Closing balance (R)");

        AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("30-day balance trend");
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.getStyleClass().add("trend-chart");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (Map.Entry<LocalDate, Double> entry : summary.balanceTrend().entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().getDayOfMonth(), entry.getValue()));
        }
        chart.getData().add(series);

        VBox panel = new VBox(chart);
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

        VBox panel = new VBox(chart);
        panel.getStyleClass().add("card");
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private static VBox buildWarningsPanel(DashboardSummary summary) {
        Label header = new Label("Warnings (most recent first)");
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

}
