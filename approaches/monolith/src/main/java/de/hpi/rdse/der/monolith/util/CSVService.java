package de.hpi.rdse.der.monolith.util;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CSVService {

    /**
     * Reads the restaurant dataset all at once and parses it into single records
     * @param dataFile The path to the restaurant dataset
     * @param splitSymbol The split symbol that is used
     * @param addIdField A flag that can be used to add an increasing id field as the first value of each record
     * @return A list of string arrays, where every string array represents a singel record of the dataset
     */
    public static List<String[]> readDataset(String dataFile, String splitSymbol, boolean addIdField) {
        List<String[]> result = new ArrayList<String[]>();
        CSVReader reader = null;

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8);
            reader = new CSVReader(inputStreamReader, '\n');

            // create header
            String[] originalHeader = reader.readNext()[0].split(splitSymbol);
            String[] header = null;
            if(addIdField) {
                // header = originalHeader + id column
                 header = new String[originalHeader.length + 1];
                header[0] = "id";
                System.arraycopy(originalHeader, 0, header, 1, originalHeader.length);
            } else {
                header = originalHeader;
            }

            String[] tmpRecord = null;
            int idCounter = 1;
            int destCopyPosition = addIdField ? 1 : 0;
            while ((tmpRecord = reader.readNext()) != null) {
                String line = tmpRecord[0].replaceAll("\"", "").replaceAll("\'", "");
                String[] split = line.split(splitSymbol);
                String[] record = new String[header.length];
                Arrays.fill(record,"");

                if(addIdField) {
                    // add id column
                    record[0] = "" + idCounter;
                    idCounter++;
                }

                // copy all other values
                System.arraycopy(split,0,record,destCopyPosition,split.length);
                result.add(record);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
