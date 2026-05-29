import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Expense_Tracker {
    private static final String FILE_NAME = "expenses.txt";
    private static final List<Expense> expenses = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ANSI Escape Codes for UI Styling
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";

    public static void main(String[] args) {
        loadExpenses(); // Load existing expenses from the file
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            saveExpenses();
            System.out.println(BOLD + BLUE + "\n********* Personal Expense Tracker *********" + RESET);
            System.out.println(CYAN + "1." + RESET + " Add Expense");
            System.out.println(CYAN + "2." + RESET + " View Expenses");
            System.out.println(CYAN + "3." + RESET + " Edit Expense");
            System.out.println(CYAN + "4." + RESET + " Delete Expense");
            System.out.println(CYAN + "5." + RESET + " Calculate Total Expenses");
            System.out.println(CYAN + "6." + RESET + " View Summary Report");
            System.out.println(CYAN + "7." + RESET + " Exit");
            System.out.print(BOLD + "Enter your choice:- " + RESET);

            while (!scanner.hasNextInt()) {
                System.out.print(RED + "Invalid input! Please Enter a number:- " + RESET);
                scanner.next();
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> addExpense(scanner);
                case 2 -> view(scanner);
                case 3 -> editExpense(scanner);
                case 4 -> deleteExpense(scanner);
                case 5 -> calculateTotal();
                case 6 -> viewSummaryReport(scanner);
                case 7 -> {
                    saveExpenses(); // Save data before exiting
                    System.out.println(GREEN + BOLD + "Saved Data Successfully To File. Goodbye!" + RESET);
                }
                default -> System.out.println(RED + "Invalid choice! Please try again." + RESET);
            }
        } while (choice != 7);
        scanner.close();
    }

    private static void addExpense(Scanner scanner) {
        System.out.println(BOLD + BLUE + "\n--- Add New Expense ---" + RESET);
        System.out.print("Enter date (YYYY-MM-DD):- ");
        String date = scanner.nextLine();
        if (date_checker(date)) {
            System.out.print("Enter amount (Rs.):- ");
            double amount;
            while (!scanner.hasNextDouble()) {
                System.out.print(RED + "Invalid input! Please enter a valid amount:- " + RESET);
                scanner.next();
            }
            amount = scanner.nextDouble();
            scanner.nextLine(); // consume newline

            System.out.print("Enter category:- ");
            String category = scanner.nextLine();

            System.out.print("Enter payment mode:- ");
            String paymentMode = scanner.nextLine();

            System.out.print("Enter description:- ");
            String description = scanner.nextLine();

            Expense expense = new Expense(date, amount, category, paymentMode, description);
            expenses.add(expense);
            System.out.println(GREEN + "Expense added successfully!" + RESET);
            Collections.sort(expenses, Comparator.comparing(Expense::getDate));
        }
    }

    private static void view(Scanner scanner) {
        System.out.println(BOLD + BLUE + "\n---- CHOOSE ONE OF THE VIEW OPTIONS ----" + RESET);
        System.out.println(CYAN + "1." + RESET + " View All Expenses");
        System.out.println(CYAN + "2." + RESET + " View Expenses By Specific Category");
        System.out.println(CYAN + "3." + RESET + " View Expenses By Specific Date");
        System.out.println(CYAN + "4." + RESET + " View Highest Expense");
        System.out.println(CYAN + "5." + RESET + " View All Expenses Sorted By Amount");
        System.out.print(BOLD + "Enter your choice:- " + RESET);
        int choice;
        try {
            if (!scanner.hasNextInt()) {
                System.out.println(RED + "Invalid Input. Must be a number." + RESET);
                scanner.next();
                return;
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            switch (choice) {
                case 1 -> viewExpenses();
                case 2 -> filterByCategory(scanner);
                case 3 -> viewExpensesByDate(scanner);
                case 4 -> viewHighestExpense();
                case 5 -> sortExpensesByAmount();
                default -> System.out.println(RED + "Invalid Choice" + RESET);
            }
        } catch (InputMismatchException e) {
            System.out.println(RED + "Invalid Input" + RESET);
        }
    }

    private static void viewExpenses() {
        if (expenses.isEmpty()) {
            System.out.println(YELLOW + "No expenses recorded yet." + RESET);
            return;
        }
        System.out.println(BOLD + CYAN + "\n=================================== Expense List ==================================" + RESET);
        printTableHeader();
        for (int i = 0; i < expenses.size(); i++) {
            expenses.get(i).printRow(i + 1);
        }
        printTableFooter();
    }

    private static void filterByCategory(Scanner scanner) {
        System.out.print("Enter category to filter: ");
        String category = scanner.nextLine();
        System.out.println(BOLD + CYAN + "\n=== Expenses in Category: " + category + " ===" + RESET);
        boolean found = false;
        printTableHeader();
        int rowNum = 1;
        for (Expense e : expenses) {
            if (e.getCategory().equalsIgnoreCase(category)) {
                e.printRow(rowNum++);
                found = true;
            }
        }
        printTableFooter();
        if (!found) {
            System.out.println(YELLOW + "No expenses found in this category." + RESET);
        }
    }

    private static void viewExpensesByDate(Scanner scanner) {
        System.out.print("Enter date (YYYY-MM-DD) to view expenses: ");
        String date = scanner.nextLine();
        if (date_checker(date)) {
            System.out.println(BOLD + CYAN + "\n=== Expenses on Date: " + date + " ===" + RESET);
            boolean found = false;
            printTableHeader();
            int rowNum = 1;
            for (Expense expense : expenses) {
                if (expense.getDate().equals(date)) {
                    expense.printRow(rowNum++);
                    found = true;
                }
            }
            printTableFooter();
            if (!found) {
                System.out.println(YELLOW + "No expenses found for this date." + RESET);
            }
        }
    }

    private static void viewHighestExpense() {
        if (expenses.isEmpty()) {
            System.out.println(YELLOW + "No expenses to display." + RESET);
        } else {
            System.out.println(BOLD + CYAN + "\n================================= Highest Expense =================================" + RESET);
            Expense highest = Collections.max(expenses, Comparator.comparingDouble(Expense::getAmount));
            printTableHeader();
            highest.printRow(1);
            printTableFooter();
        }
    }

    private static void sortExpensesByAmount() {
        if (expenses.isEmpty()) {
            System.out.println(YELLOW + "No expenses recorded yet." + RESET);
            return;
        }
        List<Expense> sorted = new ArrayList<>(expenses);
        sorted.sort(Comparator.comparingDouble(Expense::getAmount));
        System.out.println(BOLD + CYAN + "\n============================= Expenses Sorted By Amount =============================" + RESET);
        printTableHeader();
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).printRow(i + 1);
        }
        printTableFooter();
    }

    private static void calculateTotal() {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        System.out.println(BOLD + PURPLE + "Total Expenses:- Rs. " + String.format("%.2f", total) + RESET);
    }

    private static void editExpense(Scanner scanner) {
        if (expenses.isEmpty()) {
            System.out.println(YELLOW + "No expenses recorded yet to edit." + RESET);
            return;
        }
        viewExpenses();
        try {
            System.out.print("Enter the number of the expense to edit: ");
            if (!scanner.hasNextInt()) {
                System.out.println(RED + "Invalid number selection!" + RESET);
                scanner.next();
                return;
            }
            int index = scanner.nextInt() - 1;
            scanner.nextLine(); // consume newline
            if (index < 0 || index >= expenses.size()) {
                System.out.println(RED + "Invalid selection!" + RESET);
                return;
            }
            Expense selectedExpense = expenses.get(index);
            System.out.print("Enter new date (YYYY-MM-DD) or press Enter to keep (" + selectedExpense.getDate() + "): ");
            String newDate = scanner.nextLine();
            
            boolean dateValid = true;
            if (!newDate.isEmpty()) {
                dateValid = date_checker(newDate);
            }

            if (dateValid) {
                System.out.print("Enter new amount or press Enter to keep (Rs. " + selectedExpense.getAmount() + "): ");
                String amountInput = scanner.nextLine();
                
                System.out.print("Enter new category or press Enter to keep (" + selectedExpense.getCategory() + "): ");
                String newCategory = scanner.nextLine();
                
                System.out.print("Enter new payment mode or press Enter to keep (" + selectedExpense.getPaymentMode() + "): ");
                String newPaymentMode = scanner.nextLine();
                
                System.out.print("Enter new description or press Enter to keep (" + selectedExpense.getDescription() + "): ");
                String newDescription = scanner.nextLine();

                if (!newDate.isEmpty())
                    selectedExpense.setDate(newDate);
                
                if (!amountInput.isEmpty()) {
                    try {
                        selectedExpense.setAmount(Double.parseDouble(amountInput));
                    } catch (NumberFormatException e) {
                        System.out.println(YELLOW + "Invalid amount format! Keeping existing amount." + RESET);
                    }
                }
                
                if (!newCategory.isEmpty())
                    selectedExpense.setCategory(newCategory);
                
                if (!newPaymentMode.isEmpty())
                    selectedExpense.setPaymentMode(newPaymentMode);
                
                if (!newDescription.isEmpty())
                    selectedExpense.setDescription(newDescription);
                
                System.out.println(GREEN + "Expense updated successfully!" + RESET);
                Collections.sort(expenses, Comparator.comparing(Expense::getDate));
            }
        } catch (Exception e) {
            System.out.println(RED + "Error occurred in editing expense: " + e.getMessage() + RESET);
        }
    }

    private static void deleteExpense(Scanner scanner) {
        if (expenses.isEmpty()) {
            System.out.println(YELLOW + "No expenses recorded yet to delete." + RESET);
            return;
        }
        viewExpenses();
        try {
            System.out.print("Enter the number of the expense to delete: ");
            if (!scanner.hasNextInt()) {
                System.out.println(RED + "Invalid number selection!" + RESET);
                scanner.next();
                return;
            }
            int index = scanner.nextInt() - 1;
            scanner.nextLine(); // consume newline
            if (index < 0 || index >= expenses.size()) {
                System.out.println(RED + "Invalid selection!" + RESET);
                return;
            }
            expenses.remove(index);
            System.out.println(GREEN + "Expense deleted successfully!" + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error occurred in deleting expense." + RESET);
        }
    }

    private static boolean date_checker(String date) {
        try {
            LocalDate.parse(date, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            System.out.println(RED + "Incorrect Date or Date Format (Please use YYYY-MM-DD)." + RESET);
            return false;
        }
    }

    private static void viewSummaryReport(Scanner scanner) {
        System.out.println(BOLD + BLUE + "\n=== Summary Report ===" + RESET);
        System.out.println(CYAN + "1." + RESET + " Monthly Summary");
        System.out.println(CYAN + "2." + RESET + " Yearly Summary");
        System.out.print(BOLD + "Enter your choice: " + RESET);

        while (!scanner.hasNextInt()) {
            System.out.print(RED + "Invalid input! Please enter a number:- " + RESET);
            scanner.next();
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1 -> viewMonthlySummary();
            case 2 -> viewYearlySummary();
            default -> System.out.println(RED + "Invalid choice! Please try again." + RESET);
        }
    }

    private static void viewMonthlySummary() {
        if (expenses.isEmpty()) {
            System.out.println(YELLOW + "No expenses recorded yet." + RESET);
            return;
        }

        System.out.println(BOLD + CYAN + "\n========= Monthly Summary =========" + RESET);
        String currentMonth = "";
        double totalAmount = 0.0;

        for (Expense e : expenses) {
            String month = e.getDate().substring(0, 7); // YYYY-MM
            if (!month.equals(currentMonth)) {
                if (!currentMonth.isEmpty()) {
                    System.out.printf("%s: Rs. %.2f%n", currentMonth, totalAmount);
                }
                currentMonth = month;
                totalAmount = 0.0;
            }
            totalAmount += e.getAmount();
        }

        if (!currentMonth.isEmpty()) {
            System.out.printf("%s: Rs. %.2f%n", currentMonth, totalAmount);
        }
    }

    private static void viewYearlySummary() {
        if (expenses.isEmpty()) {
            System.out.println(YELLOW + "No expenses recorded yet." + RESET);
            return;
        }

        System.out.println(BOLD + CYAN + "\n========= Yearly Summary =========" + RESET);
        String currentYear = "";
        double totalAmount = 0.0;

        for (Expense e : expenses) {
            String year = e.getDate().substring(0, 4); // YYYY
            if (!year.equals(currentYear)) {
                if (!currentYear.isEmpty()) {
                    System.out.printf("%s: Rs. %.2f%n", currentYear, totalAmount);
                }
                currentYear = year;
                totalAmount = 0.0;
            }
            totalAmount += e.getAmount();
        }

        if (!currentYear.isEmpty()) {
            System.out.printf("%s: Rs. %.2f%n", currentYear, totalAmount);
        }
    }

    private static void saveExpenses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Expense e : expenses) {
                writer.write(e.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println(RED + "Error saving expenses: " + e.getMessage() + RESET);
        }
    }

    private static void loadExpenses() {
        File file = new File(FILE_NAME);
        if (!file.exists())
            return;

        expenses.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    Expense expense = Expense.fromFileString(line);
                    expenses.add(expense);
                } catch (Exception e) {
                    System.out.println(YELLOW + "Skipping invalid data line: " + line + RESET);
                }
            }
            Collections.sort(expenses, Comparator.comparing(Expense::getDate));
        } catch (IOException e) {
            System.out.println(RED + "Error loading expenses: " + e.getMessage() + RESET);
        }
    }

    // Helper methods for table formatting
    private static void printTableHeader() {
        System.out.println(CYAN + "+-----+------------+------------+--------------------+--------------------+--------------------------------+" + RESET);
        System.out.printf(CYAN + "| %-3s | %-10s | %-10s | %-18s | %-18s | %-30s |%n" + RESET,
                "No.", "Date", "Amount", "Category", "Payment Mode", "Description");
        System.out.println(CYAN + "+-----+------------+------------+--------------------+--------------------+--------------------------------+" + RESET);
    }

    private static void printTableFooter() {
        System.out.println(CYAN + "+-----+------------+------------+--------------------+--------------------+--------------------------------+" + RESET);
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}

class Expense {
    private String date;
    private double amount;
    private String category;
    private String paymentMode;
    private String description;

    public Expense(String date, double amount, String category, String paymentMode, String description) {
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.paymentMode = paymentMode;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Print a single aligned row for ASCII table
    public void printRow(int index) {
        System.out.printf("| %-3d | %-10s | Rs.%-7.2f | %-18s | %-18s | %-30s |%n",
                index, date, amount, 
                Expense_Tracker.truncate(category, 18), 
                Expense_Tracker.truncate(paymentMode, 18), 
                Expense_Tracker.truncate(description, 30));
    }

    @Override
    public String toString() {
        return date + "\t\tRS." + amount + "\t\t" + category + "\t\t" + paymentMode + "\t\t" + description;
    }

    // RFC 4180 compliant CSV formatting
    public String toFileString() {
        return date + "," + amount + "," + escapeCSV(category) + "," + escapeCSV(paymentMode) + "," + escapeCSV(description);
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // RFC 4180 compliant CSV parser
    public static Expense fromFileString(String fileString) {
        List<String> parts = parseCSV(fileString);
        if (parts.size() < 5) {
            throw new IllegalArgumentException("Invalid CSV row");
        }
        return new Expense(parts.get(0), Double.parseDouble(parts.get(1)), parts.get(2), parts.get(3), parts.get(4));
    }

    private static List<String> parseCSV(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"'); // escaped quote
                    i++;
                } else {
                    inQuotes = !inQuotes; // toggle quote state
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }
}