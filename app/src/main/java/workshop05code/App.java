package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
// Included for the logging exercise
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            System.out.println("Wordle created and connected.");
        } else {
            System.out.println("Not able to connect. Sorry!");
            return;
        }
        if (wordleDatabaseConnection.createWordleTables()) {
            System.out.println("Wordle structures in place.");
        } else {
            System.out.println("Not able to launch. Sorry!");
            return;
        }

        // ✅ Validate and add words from file
        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.matches("[a-z]{4}")) {
                    logger.info("Loaded valid word from file: " + line);
                    wordleDatabaseConnection.addValidWord(i, line);
                } else {
                    logger.severe("Invalid word in data.txt: " + line);
                    System.out.println("Ignored invalid word from file: " + line);
                }
                i++;
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading data.txt", e);
            System.out.println("Not able to load. Sorry!");
            return;
        }

        // ✅ Get user input and validate before checking DB
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4 letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                if (!guess.matches("[a-z]{4}")) {
                    System.out.println("Invalid input. Please enter a lowercase 4-letter word.");
                    logger.warning("Invalid guess entered: " + guess);
                } else {
                    System.out.println("You've guessed '" + guess + "'.");
                    if (wordleDatabaseConnection.isValidWord(guess)) {
                        System.out.println("Success! It is in the list.\n");
                    } else {
                        System.out.println("Sorry. This word is NOT in the list.\n");
                    }
                }

                System.out.print("Enter a 4 letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.SEVERE, "Scanner error", e);
        }
    }
}
