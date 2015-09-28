/*
 * Copyright (c) 2015 Annie Hui @ RStar Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rstar.mobile.thermocouple.tables;

import com.rstar.mobile.thermocouple.AppSettings;
import com.rstar.mobile.thermocouple.Savelog;


public class TableParser {
    private static final String TAG = TableParser.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;


    private double[] getTableEntryLine(String line) {
        if (line==null || line.length()==0) return null;

        line = line.trim();

        if (line.length()==0) return null;

        String[] fields = line.split("\\s+");  // use one or more whitespaces as delimiter

        // a valid line must have 2 to 12 fields
        if (fields==null || fields.length>12 || fields.length<2) return null;

        try {
            Double.parseDouble(fields[0]);
        }
        catch (NumberFormatException e) {
            // the first field is not a number. 
            return null;
        }

        double[] data = new double[fields.length];
        for (int j=0; j<fields.length; j++) {
           // In the table, every field has at most length 6. 
           if (fields[j].length() > 6) return null;  
           try {
             data[j] =  Double.parseDouble(fields[j]);
           }
           catch (Exception e) {
               return null;   // something wrong. Not a valid field.
           }
        }
        return data;
    }

    public double[][] parse(String data) {
        if (data==null || data.length()==0) return null;

        String[] lines = data.split("\\n");

        double[][] slots = new double[lines.length][];

        // Get the lines that are table entries
        int count = 0;
        for (int i=0; i<lines.length; i++) {
            double numericData[] = getTableEntryLine(lines[i]);
            if (numericData!=null) {
                slots[count] = numericData;
                count++;
            }
        }

        Savelog.d(TAG, debug, "Number of lines = " + lines.length);
        Savelog.d(TAG, debug, "Number of useful lines = " + count);

        return interpretEntries(slots, count);
    }

    private double[][] interpretEntries(double[][] slots, int count) {
        // For each row of table entries,
        // the first field is supposed to be T


        boolean[] positive = new boolean[count];
        for (int row=0; row<count; row++) {
            if (slots[row][0] < -0.1) { 
                // T is negative
                positive[row] = false;
            }
            else if (slots[row][0] > 0.1) {
                // T is positive
                positive[row] = true;
            }
            else if (row<count-1 && Math.abs(slots[row][0]-slots[row+1][0])<0.1) {
                // T is 0 and the next line is also 0
                positive[row] = false;
            }
            else {
                // T is 0 and the next line is not 0
                positive[row] = true;
            }
        }

        int maxEntries = 0;
        for (int row=0; row<count; row++) {
            maxEntries += slots[row].length;
        }
        double[][] tableEntries = new double[2][maxEntries];
        
        
        int numberOfEntries = 0;
        for (int row=0; row<count; row++) {
            // First entry of each row is T
            double T = slots[row][0];

            if (positive[row]) {
                // Subsequent entries are E
                for (int pos=1; pos<slots[row].length; pos++) {
                    double Toffset = pos - 1.0;

                    if (pos==1 && row!=0) {
                        int previousRowLastEntry = slots[row-1].length - 1;
                        if (slots[row][pos]==slots[row-1][previousRowLastEntry]) {
                            // Repeated
                        }
                        else if (Math.abs(slots[row][pos])<0.1 && Math.abs(slots[row-1][pos])<0.1) {
                            // Repeated zero
                        }
                        else {
                            // Not repeated. Quite ununusal. Report this.
                            Savelog.d(TAG, debug, "Warning: Entry (" + row + "," + pos + ") E=" + slots[row][pos] + " not repeated.");
                            // Record the entry
                            tableEntries[0][numberOfEntries] = T + Toffset;
                            tableEntries[1][numberOfEntries] = slots[row][pos];
                            numberOfEntries++;
                        }
                    }
                    else {
                        // Record the entry
                        tableEntries[0][numberOfEntries] = T + Toffset;
                        tableEntries[1][numberOfEntries] = slots[row][pos];
                        numberOfEntries++;
                    }
                }
            }
            else { // Entries are filled in reverse order 
                for (int pos=slots[row].length-1; pos>=1; pos--) {
                    double Toffset = -(pos - 1.0);

                    if (pos==slots[row].length-1 && row!=0) {
                        int previousRowFirstEntry = 1;
                        if (slots[row][pos]==slots[row-1][previousRowFirstEntry]) {
                            // Repeated
                        }
                        else {
                           // Not repeated. Quite ununusal. Report this.
                            Savelog.d(TAG, debug, "Warning: Entry (" + row + "," + pos + ") E=" + slots[row][pos] + " not repeated.");
                            // Record the entry
                            tableEntries[0][numberOfEntries] = T + Toffset;
                            tableEntries[1][numberOfEntries] = slots[row][pos];
                            numberOfEntries++;
                        }
                    }
                    else {
                        // Record the entry
                        tableEntries[0][numberOfEntries] = T + Toffset;
                        tableEntries[1][numberOfEntries] = slots[row][pos];
                        numberOfEntries++;
                    }
                }
            }
        }


        double [][] finalTable = new double[2][numberOfEntries];

        for (int i=0; i<2; i++) {
            for (int j=0; j<numberOfEntries; j++) {
                finalTable[i][j] = tableEntries[i][j];
            }
        }
        return finalTable;
    }

    public String getTableAsString(double[][] table) {
        String data = "";
        for (int i=0; i<table[0].length; i++) {
            if (Math.abs( (int)(table[0][i]) % 10)== 0) {
                String Tstring = String.format("%.1f", table[0][i]);
                data += "\n";
                data += Tstring;
            }
            String Estring = String.format(" %.3f", table[1][i]);
            data += Estring;
        }
        data += "\n";
        return data;
    }

}
