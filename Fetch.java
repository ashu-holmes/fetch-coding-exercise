//import statements
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Fetch {
    private static final String CSV_SEPARATOR = ",";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static void main(String[] args) throws IOException {
        //Read Arguments 1-Points To Spend and 2-File Name
        int points = Integer.parseInt(args[0]);
        String file_name = args[1];

        //Read all the transactions from CSV file
        List<Transaction> transactions = read_transactions(file_name);
        //compute final balances of each payer
        Map<String, Integer> payer_balances = spending_points(transactions, points);

        // Print all the final balances
        System.out.println(payer_balances);
    }
    
    private static List<Transaction> read_transactions(String fileName) throws IOException {
        //To store the list of all the transactions
        List<Transaction> transactions = new ArrayList<>();
        
        //To read from the csv file
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        // Skip the header line
        String line = reader.readLine();
        
        //read each transaction in the csv file
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(CSV_SEPARATOR);
            //Payer Name
            String payer = fields[0].substring(1, fields[0].length() - 1);
            //Read Points of the transaction
            int points = Integer.parseInt(fields[1]);
            //Read timestamp
            String date_time = fields[2].substring(1, fields[2].length() - 1);
            LocalDateTime timestamp = LocalDateTime.parse(date_time, DATE_TIME_FORMATTER);
            //create a new Transaction object
            Transaction transaction = new Transaction(payer, points, timestamp);
            transactions.add(transaction);
        }
        
        reader.close();
        //return the final list of all transactions
        return transactions;
    }//end of method read_transactions
    
    private static Map<String, Integer> spending_points(List<Transaction> transactions, int rem_points) {
        // Sort the transactions by timestamp (oldest first)
        Collections.sort(transactions);

        // To track current balances of each payer
        Map<String, Integer> payer_balances = new HashMap<>();

        //Go through each transaction
        for (Transaction transaction : transactions) {
            String payer = transaction.getPayer();
            int points = transaction.getPoints();
            // Update the payer's balance
            int balance = payer_balances.getOrDefault(payer, 0);
            //No payer's balance point should go negative 
            if(balance+points<0) 
            {
             //Return some of the points to the payer to avoid negative value   
             if(payer_balances.containsKey(payer))   
             rem_points = rem_points - (balance+points);
             else
             payer_balances.put(payer,0);
             //Move to next transaction
             continue;
            }
            //update payer point balance
            payer_balances.put(payer, balance + points);

            // Spend the points and update the payer's balance as well as remaining spend points
            if (rem_points > 0) {
                int points_spent = Math.min(rem_points, balance+points);
                rem_points -= points_spent;
                payer_balances.put(payer, balance + points - points_spent);
            }
        }

        return payer_balances;
    }//end of method spending_points   
}//end of class Fetch
class Transaction implements Comparable<Transaction> {
    private final String payer;
    private final int points;
    private final LocalDateTime timestamp;

    public Transaction(String payer, int points, LocalDateTime timestamp) {
        this.payer = payer;
        this.points = points;
        this.timestamp = timestamp;
    }

    public String getPayer() {
        return payer;
    }

    public int getPoints() {
        return points;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    //compare Transcation on the basis of timestamp
    @Override
    public int compareTo(Transaction other) {
        return timestamp.compareTo(other.timestamp);
    }
}
