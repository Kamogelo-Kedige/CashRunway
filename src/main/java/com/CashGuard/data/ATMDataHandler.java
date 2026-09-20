package com.CashGuard.data;

import com.CashGuard.model.ATM;
import com.CashGuard.model.ATMDayLog;
import com.CashGuard.model.Denomination;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a single ATM's data file and turns it into an ATM Object.
 */
public class ATMDataHandler
{
    //Add a method to read in a file containing transactions from the atm
    public static ATM loadDataFromFile(String path, double maxCashCapacity)
    {
        //list of transactions from atm
        List<ATMDayLog> logs = new ArrayList<>();
        //ATM particulars
        String id = null;
        String location = "";

        //capacity guard
        if(maxCashCapacity <= 0)
            throw new IllegalArgumentException("Max cash capacity must be greater than 0.");

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(path)))
        {

            // skip header line
            String line = bufferedReader.readLine();

            while ((line = bufferedReader.readLine()) != null)
            {
                String[] tokens = line.split(",");

                //get atm id and its location once on the first row, doesn't change on each row
                if(id == null)
                {
                    id = tokens[0];
                    location = tokens[1];
                }

                //get rest of the data from csv file
                ATMDayLog log = getTransaction(tokens);
                //add to list of day logs
                logs.add(log);

            }

        }
        catch (IllegalArgumentException | IOException e) {
            throw new IllegalStateException("Unable to load ATM data from " + path, e);
        }

        return new ATM(id,location,maxCashCapacity, logs);
    }

    //helper method to get the rest of the data from the remaining columns
    private static ATMDayLog getTransaction(String[] tokens) {
        ATMDayLog transaction = new ATMDayLog();


        transaction.setDate(LocalDate.parse(tokens[2]));
        transaction.setWithdrawalAmount(Double.parseDouble(tokens[3]));
        transaction.setDenominationBreakdown(new Denomination(
                Double.parseDouble(tokens[4]),
                Double.parseDouble(tokens[5]),
                Double.parseDouble(tokens[6]),
                Double.parseDouble(tokens[7]),
                Double.parseDouble(tokens[8])
        ));

        transaction.setDeposits(Double.parseDouble(tokens[9]));
        transaction.setClosingBalance(Double.parseDouble(tokens[10]));
        transaction.setDowntime(Boolean.parseBoolean(tokens[11]));
        transaction.setPayday(Boolean.parseBoolean(tokens[12]));
        transaction.setMonthEnd(Boolean.parseBoolean(tokens[13]));
        transaction.setPublicHoliday(Boolean.parseBoolean(tokens[14]));
        return transaction;
    }

}
