package currencyconverter;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sporax
 */

/* 
 * Defines a Units class, which allows conversion between two major units types: Indian formats
 * and American formats (lakh, crore, etc. to million, billion, etc.). Stores these as ISF and 
 * USF, static variables available for reference by other classes.
 * Also allows creation of other units types if required.
 
 * Useful methods include: toDigits, which returns a digit value of a string, and toWords, which
 * returns a string value of a condensed number.
 */

public class Units {
    // ISF and USF are class constants, equal to "indian standard format" and "uS standard format"
    public final static Units ISF;
    public final static Units USF;
    
    // units stores the string representation of a unit, for example, "thousand"
    private final ArrayList<String> units;
    // digits stores the integer representation of a unit, for example, 1000
    private final ArrayList<Integer> digits;    
    
    // initialize static variables ISF and USF, which are used by other classes
    static {
        String[] inQuantity = {"-", "thousand", "lakh", "crore"};
        int[] inValues = {1, 1000, 100000, 10000000};
        ISF = new Units(inQuantity, inValues);
        String[] usQuantity = {"-", "thousand", "million", "billion"};
        int[] usValues = {1, 1000, 1000000, 1000000000};
        USF = new Units(usQuantity, usValues);
    }
    
    // Converts given int array to an Integer array, and stores values as parallel
    // arrays for easy reference. Requires that parameters units and values are the
    // same length and that the units are in ascending order.
    public Units(String[] units, int[] values) {
        // convert int array to Integer array
        Integer[] newValues = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            newValues[i] = values[i];
        }
        // create new parallel lists
        this.units = new ArrayList<>(Arrays.asList(units));
        this.digits = new ArrayList<>(Arrays.asList(newValues));
    }
    
    // Converts a (large) floating point number into a String of a number and 
    // a unit. Formats the decimal to 6 places and returns the String. 
    // Eg. 1200 -> "1.20000 thousand"
    public String toWords(double value) {
        String result = "";
        for (int i = digits.size() - 1; i >= 0; i--) {
            if (value >= digits.get(i)) {
                value /= digits.get(i);
                result = units.get(i);
                break;
            }
        }
        DecimalFormat df = new DecimalFormat("#.00000");
        result = result.equals("-") ? df.format(value) : df.format(value) + " " + result;
        if (result.startsWith("."))
            result = "0" + result;
        return result;
    }
    
    // Converts a given String value into an integer and returns this value. For example,
    // "thousand" -> 1000. if no conversion is stored, returns 1.
    public int toDigits(String value) {
        return units.indexOf(value) >= 0 ? digits.get(units.indexOf(value)) : 1;
    }
    
    // Returns the quantities present in the units
    public List<String> getQuantities() {
        return new ArrayList<String>(units);  // don't expose private variables publically
    }
    
    // toString method used for testing purposes
    @Override
    public String toString() {
        return "units: " + units + ", digits: " + digits;
    }
}
