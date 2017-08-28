package currencyconverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sporax
 */

/* 
 * This defines a Currency class, which allows a currency to be stored using its
 * name, type (Indian format or US format), and rate of conversion. Implements 
 * Units objects for type storage (ISF/USF). Allows the user to get a stored rate
 * of conversion, set a new rate, get the format of a currency, and to initialize
 * all currencies.
 
 > Text files rates.txt and currencies.txt are used for high-level initialization of
 > all currencies. every currency name stored in currencies.txt is initialized as a new
 > currency when initializeAllCurrencies() is called, and each currency's conversion rate is
 > set using rates from rates.txt. initializeAllCurrencies() returns a map of string to currency
 > object which can be used for client purposes.

 * DOCUMENTATION OF METHODS:
 +  currency(String name, String type): Initializes new currency with given name and type
 -  initializeRates():                  Gets rates from rates.txt, adds conversions for this
                                        currency to conversionRate (map)
 +  addToDatabase():                    Adds this currency to currencies.txt if it doesn't already exist
 +  removeFromDatabase():               Removes this currency from currencies.txt
 +  convertsTo(String other):           Returns whether this currency converts to the other one
 +  getRate(String other):              Returns rate -- assumes it converts. will cause errors otherwise
 +  setRate(String other, int rate):    Stores a rate in rates.txt. updates the rate if the conversion
                                        already existed, or creates a new rate.
 +  getFormatAsString():                Returns format of currency as string, either "ISF" or "USF"
 -S  getFilename(String filename):       Returns a padded String representing a filename
 +  clearRates():                       Deletes entries of this currency from rates.txt
 -  clearFile(String filename):         Deletes all lines starting with this.name from file
 +  clearAllRates():                    Removes all rates from rates.txt
 d  toString():                         Returns a string representation of the object (testing purposes)
 +S initializeAllCurrencies():          Described in detail above
 + removeBlankLines():                  Removes all blank lines from both currencies.txt and rates.txt
 - removeBlankLines(String filename):   Implements removeBlankLines()

(key: S=static, +=public, -=private, d=debugging)
 */

public class Currency {
    private static final String RATES_FILENAME = System.getProperty("user.home") + "/.rates.txt";
    private static final String CURRENCIES_FILENAME = System.getProperty("user.home") + "/.currencies.txt";
    private String name;
    private String type;
    private TreeMap<String, Double> conversionRate;
    
    // Stores name of currency, type of currency and stores any stored conversion rates
    // in a map
    public Currency(String name, String type) {
        this.name = name.toUpperCase();
        this.type = type;
        conversionRate = new TreeMap<>();
        this.initializeRates();
    }
    
    // Fetches rates from rates.txt that convert from this currency to other currencies.
    // Stores other currency names and rate of conversion in conversionRate for later use
    private void initializeRates() {
        try {
            Scanner lineScanner = new Scanner(new File(RATES_FILENAME));
            while (lineScanner.hasNextLine()) {
                String line = lineScanner.nextLine();
                // split the line: "from:to:rate" -> ["from", "to", "rate"]
                String[] tokens = line.split(":");
                // fetch rates converting from this currency
                if (tokens[0].equals(name)) {
                    // store conversion values
                    conversionRate.put(tokens[1], Double.parseDouble(tokens[2]));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Currency.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Adds this currency to currencies.txt if not already in currencies.txt
    public void addToDatabase() {
        try {
            boolean exists = false;
            Scanner input = new Scanner(new File(CURRENCIES_FILENAME));
            String currencies = "";
            while (input.hasNextLine()) {
                String line = input.nextLine();
                currencies += line + "\n";  // currencies stores all data from currencies.txt
                if (line.substring(0, 3).equals(name))
                    exists = true;
            }
            if (!exists) {
                // write a new entry to currencies.txt
                currencies += "\n" + name + ":" + getFormatAsString().toLowerCase();
                PrintStream output = new PrintStream(new File(CURRENCIES_FILENAME));
                output.print(currencies);
            }
        } catch (NullPointerException | NoSuchElementException | FileNotFoundException ex) {
            Logger.getLogger(Currency.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Removes this currency from currencies.txt
    public void removeFromDatabase() {
        clearFile(CURRENCIES_FILENAME);  // clearFile handles filename padding
    }
    
    // Returns true if this currency converts to the other currency, false otherwise
    public boolean convertsTo(String otherCurrency) {
        return conversionRate.keySet().contains(otherCurrency);
    }
    
    // pre: this converts to other
    // Returns the conversion rate from this currency to provided currency as a double
    public double getRate(String otherCurrency) {
        return conversionRate.get(otherCurrency);
    }
    
    // Stores a conversion rate in rates.txt for the given currency. 
    // Adds a new value if the conversion did not previously exist, otherwise
    // updates the current value.
    public void setRate(String otherCurrency, double rate) {
        otherCurrency = otherCurrency.toUpperCase();
        // get contents of text file
        try {
//            String[] rates = new Scanner(getClass().getResourceAsStream("rates.txt"))
//                                        .useDelimiter("\\A").next().split("\n");
//            String[] rates = new Scanner(new File(getFilename("rates.txt")))
//                                        .useDelimiter("\\A").next().split("\n");
            String[] rates = new String(Files.readAllBytes(Paths.get(RATES_FILENAME)))
                                .split("\n");
            // if the other currency already exists, update rate
            if (convertsTo(otherCurrency)) {
                // search all rates for a matching conversion
                for (int i = 0; i < rates.length; i++) {
                    if (rates[i].startsWith(name + ":" + otherCurrency)) {
                        // if matched, then substring and add the new rate
                        rates[i] = rates[i].substring(0, 8) + rate;
                    }
                }
            } else {
                // add a line to text file for currency conversion
                String[] temp = new String[rates.length + 1];
                System.arraycopy(rates, 0, temp, 0, rates.length);
                temp[temp.length - 1] = name + ":" + otherCurrency + ":" + rate;
                rates = temp;
            }
            // delete old rates file
            new File(RATES_FILENAME).delete();
            // create new rates file
            PrintStream output = new PrintStream(new File(RATES_FILENAME));
            // print the rates array to the file
            for (String line : rates) {
                if (!line.trim().equals(""))
                    output.println(line);
            }
        } catch (NullPointerException | NoSuchElementException | IOException ex) {
            Logger.getLogger(Currency.class.getName()).log(Level.SEVERE, null, ex);
        }
        initializeRates();
    }
    
    // Returns currency type as a String, either ISF or USF
    public String getFormatAsString() {
        return type;
    }
    
    // Returns currency type as a Units object, either Units.ISF or Units.USF
    public Units getFormat() {
        return type.equalsIgnoreCase("usf") ? Units.USF : Units.ISF;
    }
    
//    // Returns a padded filename that directs to the file in the user's directory
//    private static String getFilename(String filename) {
//        return System.getProperty("user.home") + "/" + filename;
//    }
    
    // Deletes the entries for this currency from rates.txt
    public void clearRates() {
        clearFile(RATES_FILENAME);  // clearFile handles filename padding
    }
    
    // Deletes all lines starting with name from file. Auto-pads filenames.
    private void clearFile(String filename) {
        try {
            String[] rates = new String(Files.readAllBytes(Paths.get(filename))).split("\n");
            // remove all entries starting with name
            for (int i = 0; i < rates.length; i++) {
                if (rates[i].startsWith(name))
                    rates[i] = "";
            }
            // delete old rates file
            new File(filename).delete();
            // create new rates file
            PrintStream output = new PrintStream(new File(filename));
            // print the rates array to the file
            for (String line : rates) {
                if (!line.trim().equals(""))
                    output.println(line);
            }
        } catch (NullPointerException | NoSuchElementException | IOException ex) {
            Logger.getLogger(Currency.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Deletes all currency entries from rates.txt
    public static void clearAllRates() {
        try {
            File f = new File(RATES_FILENAME);
            f.delete();
            new PrintStream(f).println();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Currency.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // toString method used for testing purposes
    @Override
    public String toString() {
        return "name: " + name + ", type: " + type + ", rates: " + conversionRate;
     }
    
    // Removes blank lines from text files
    public static void removeBlankLines() {
        removeBlankLines(CURRENCIES_FILENAME);
        removeBlankLines(RATES_FILENAME);
    }
    
    // Helper method for removeBlankLines(). Removes all blank lines
    // from a given file. Auto-pads filename.
    private static void removeBlankLines(String file) {
        try {
            String contents = new String(Files.readAllBytes(Paths.get(file)));
            // deal with repeat cases
            while (contents.contains("\n\n"))
                contents = contents.replace("\n\n", "\n");
            // deal with front case
            if (contents.startsWith("\n"))
                contents = contents.substring(1);
            PrintStream output = new PrintStream(new File(file));
            output.print(contents);
        } catch (NullPointerException | NoSuchElementException | IOException ex) {
            Logger.getLogger(Currency.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Creates a database of hidden files "rates.txt" and "currencies.txt" in the user's home folder
    // if they don't already exist
    public static void createDatabase() {
        // DON'T MODIFY HARDCODED FILENAMES:
        makeFile("rates.txt");
        makeFile("currencies.txt");
    }
    
    private static void makeFile(String filename) throws NullPointerException {
        // TODO:: USE NEW VERSION
        File f = new File(System.getProperty("user.home") + "/." + filename);
//        File f = new File(System.getProperty("user.home") + "/" + filename);
        if (!f.exists()) {
            try {
                String contents = new Scanner(Currency.class.getResourceAsStream("/" + filename))
                        .useDelimiter("\\A").next();
                new PrintStream(f).println(contents);
            } catch (NoSuchElementException | FileNotFoundException ex) {
                Logger.getLogger(Currency.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // Instantiates Currency objects for all currencies listed in currencies.txt
    // Stores name and currency object in a map and returns the map.
    public static LinkedHashMap<String, Currency> initializeAllCurrencies() {
        removeBlankLines();
        LinkedHashMap<String, Currency> currencies = new LinkedHashMap<>();
        try {
            // get all unique currencies and make new Currency
            Scanner lineScanner = new Scanner(new File(CURRENCIES_FILENAME));
            while (lineScanner.hasNextLine()) {
                // tokens = [name, type]
                String[] tokens = lineScanner.nextLine().split(":");
                if (!currencies.keySet().contains(tokens[0])) {
                    Currency temp = new Currency(tokens[0], tokens[1]);
                    currencies.put(tokens[0], temp);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Currency.class.getName()).log(Level.SEVERE, null, ex);
        }
        return currencies;
    }
}
