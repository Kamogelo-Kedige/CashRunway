package com.CashGuard;

import com.CashGuard.userInterface.DashboardUI;
import com.CashGuard.analysis.Dashboard;
import com.CashGuard.data.ATMDataHandler;
import com.CashGuard.model.ATM;
import com.CashGuard.model.DashboardSummary;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;


public class Main extends Application {
    public static void main(String[] args) {

       launch(args);
       


    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     * @throws Exception if something goes wrong
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        Dashboard dashboard = new Dashboard();

        // LinkedHashMap keeps insertion order, so the ATM selector
        // lists them Mall, Township, Taxi Rank — not shuffled.
        Map<String, DashboardSummary> summaries = new LinkedHashMap<>();

        ATM mall = ATMDataHandler.loadDataFromResource("/atm_mall.csv", 500000);
        summaries.put(mall.getLocation(), dashboard.build(mall));


        ATM township = ATMDataHandler.loadDataFromResource("/atm_township.csv", 300000);
        summaries.put(township.getLocation(), dashboard.build(township));


        ATM taxiRank = ATMDataHandler.loadDataFromResource("/atm_taxirank.csv", 150000);
        summaries.put(taxiRank.getLocation(), dashboard.build(taxiRank));

        primaryStage.setScene(DashboardUI.buildMainScene(summaries));
        primaryStage.setTitle("CashRunway");
        primaryStage.show();
    }
}
