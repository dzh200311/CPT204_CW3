/******************************************************************************
 *  
 *  A library from Algorithms optional textbook
 *  to read in data of various types from: stdin, file, URL.
 *
 *  You need to code your own way for your CW3 program to read the input file.
 *
 ******************************************************************************/


import java.io.*;



public class In {
    private BufferedReader reader;
    private static final String NEWLINE = System.getProperty("line.separator");

    public In() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }


    public String readLine() {
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            System.err.println("Error occurred while reading a line: " + e.toString());
        }
        return line;
    }


    public String readAll() {
        StringBuilder allInput = new StringBuilder();
        String line;
        try {
            while ((line = readLine()) != null) {
                allInput.append(line).append(NEWLINE);
            }
        } catch (Exception e) {
            System.err.println("Error occurred while reading all input: " + e.getMessage());
        }
        return allInput.toString();
    }


    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                System.err.println("Error closing the reader: " + e.getMessage());
            }
        }
    }
}

