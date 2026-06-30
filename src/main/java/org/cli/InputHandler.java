package org.cli;

import java.util.Scanner;

public class InputHandler {
    private final Scanner scanner;

    public InputHandler() {
        this.scanner = new Scanner(System.in);
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Помилка: введіть ціле число.");
            }
        }
    }

    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public void waitForEnter() {
        System.out.print("Натисніть Enter, щоб продовжити...");
        scanner.nextLine();
    }

    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("y") || input.equals("yes") || input.equals("так") || input.equals("т")) {
                return true;
            } else if (input.equals("n") || input.equals("no") || input.equals("ні") || input.equals("н")) {
                return false;
            } else {
                System.out.println("Помилка: невідома відповідь. Будь ласка, введіть 'так' або 'ні'.");
            }
        }
    }
}
