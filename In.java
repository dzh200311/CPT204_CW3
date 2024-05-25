/******************************************************************************
 *
 *  A library from Algorithms optional textbook
 *  to read in data of various types from: stdin, file, URL.
 *
 *  You need to code your own way for your CW3 program to read the input file.
 *
 ******************************************************************************/


import java.io.*;
import java.util.Scanner;


public class In {
    private BufferedReader reader;
    private static final String NEWLINE = System.getProperty("line.separator");

    // Default constructor, read dungeon map file
    public In() {
        File directory = new File("./Dungeons"); // 设置默认目录
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null && files.length > 0) {
                System.out.println("Available dungeon maps:");
                for (int i = 0; i < files.length; i++) {
                    System.out.print((i + 1) + ": " + files[i].getName() + "\t");
                    if ((i + 1) % 6 == 0) {
                        System.out.println();
                    }
                }
                if (files.length % 6 != 0) {
                    System.out.println();
                }
                System.out.print("Enter the number of the map to load: ");
                Scanner scanner = new Scanner(System.in);
                int choice = scanner.nextInt() - 1;
                scanner.close();
                if (choice >= 0 && choice < files.length) {
                    try {
                        this.reader = new BufferedReader(new FileReader(files[choice]));
                    } catch (FileNotFoundException e) {
                        System.out.println("File not found: " + files[choice].getName());
                        System.exit(1);
                    }
                } else {
                    System.out.println("Invalid choice.");
                    System.exit(1);
                }
            } else {
                System.out.println("No text files found in the directory.");
                System.exit(1);
            }
        } else {
            System.out.println("Directory not found.");
            System.exit(1);
        }
    }

    // Read a line
    public String readLine() {
        try {
            return this.reader.readLine();
        } catch (IOException e) {
            System.err.println("Error reading line from input: " + e.getMessage());
            return null;
        }
    }

    // turn off BufferedReader
    public void close() {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                System.err.println("Error closing reader: " + e.getMessage());
            }
        }
    }}

