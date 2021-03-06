package de.hpi.rdse.der.data;

import au.com.bytecode.opencsv.CSVReader;
import de.hpi.rdse.der.setoperations.SetOperations;

import java.io.IOException;
import java.util.*;

public class CSVService {

    private boolean allDataRead;
    private Map<Integer, Queue<String>> data;
    private int maxQueuSize;
    private Set<Integer> queueSizes;
    private CSVReader csvReader;
    private long id;

    /**
     * Initializes a new object of type CSVService
     * @param dataFile the path to the file that should be read
     * @param hasHeader indicates whether the first line of that file is the header
     * @param separator the char that is used to separate all values of a single record
     * @param minBlockSize the minimal number of lines within a datachunk
     * @param maxQueueSize the maximum size of the queues that are used to provide datachunks of specific sizes
     */
    public CSVService(String dataFile, boolean hasHeader, char separator, int minBlockSize, int maxQueueSize) {
        this.allDataRead = false;

        this.data = new HashMap<>();

        this.maxQueuSize = maxQueueSize;

        this.queueSizes = new HashSet<>();
        this.queueSizes.add(minBlockSize);

        this.csvReader = CSVReaderFactory.createCSVReader(dataFile, separator);

        this.id = 1;

        if (hasHeader) {
            skipHeader();
        }

        this.fillQueues();
    }

    /**
     * Indicates if there is data available to read
     * @return True if there is data available
     */
    public boolean dataAvailable() {
        if (allQueuesEmpty()) {
            fillQueues();
            return !allQueuesEmpty();
        } else {
            return true;
        }
    }

    /**
     * Returns a specific number of lines as a single datachunk
     * @param lines The number of lines that should be read
     * @return a String consisting of n lines of the dataset to be read
     */
    public String getRecords(int lines) {
        this.queueSizes.add(lines);

        if (!sizeAvailable(lines)) {
            fillQueues();
            if(!sizeAvailable(lines)) {
                return getNextFittingDataBlock(lines);
            }
        }
        return this.data.get(lines).poll();
    }

    private void fillQueues() {
        Set<Integer> currentQueueSizes = this.data.keySet();
        Set<Integer> missingSizes = SetOperations.setDiff(this.queueSizes, currentQueueSizes);
        for (int queueSize: missingSizes) {
            this.data.put(queueSize, new LinkedList<>());
        }

        try {
            for (Integer numberOfLines: this.data.keySet()) {
                Queue<String> queue = this.data.get(numberOfLines);

                while(!allDataRead && queue.size() < this.maxQueuSize) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < numberOfLines; i++) {
                        String[] tmpRecord = this.csvReader.readNext();
                        if (tmpRecord != null) {
                            sb.append(this.id);
                            sb.append(",");
                            sb.append(tmpRecord[0]);
                            sb.append("\n");
                        } else {
                            allDataRead = true;
                            break;
                        }
                        this.id++;
                    }
                    queue.add(sb.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean allQueuesEmpty() {
        for (Queue<String> dataQueue: this.data.values()) {
            if (dataQueue.size() > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean sizeAvailable(int maxSize) {
        return this.data.get(maxSize) != null && this.data.get(maxSize).peek() != null;
    }

    private String getNextFittingDataBlock(int maxSize) {
        List<Integer> sizes = new LinkedList<>(this.data.keySet());
        Collections.sort(sizes);

        for (int i = sizes.size() - 1; i >= 0; i--) {
            int size = sizes.get(i);
            if(size < maxSize && this.data.get(size).peek() != null) {
                return this.data.get(size).poll();
            }
        }

        for (int size : sizes) {
            if (size > maxSize && this.data.get(size).peek() != null) {
                return this.data.get(size).poll();
            }
        }

        return null;
    }

    private void skipHeader() {
        try {
            this.csvReader.readNext();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
