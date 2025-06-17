package com.day2.bank;

import java.util.HashMap;
import java.util.Scanner;

public class BankSystem {
    private static HashMap<String, BankAccount> accounts = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n1. Create Account\n2. Deposit\n3. Withdraw\n4. Check Balance\n5. Exit");
            System.out.print("Choose: ");
            int choice = scanner.nextInt();
            scanner.nextLine();  // consume newline

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter account number: ");
                        String accNum = scanner.nextLine();
                        System.out.print("Enter name: ");
                        String name = scanner.nextLine();
                        if (accounts.containsKey(accNum)) {
                            throw new InvalidTransactionException("Account already exists.");
                        }
                        accounts.put(accNum, new BankAccount(accNum, name));
                        System.out.println("✅ Account created.");
                        break;

                    case 2:
                        System.out.print("Account number: ");
                        accNum = scanner.nextLine();
                        System.out.print("Amount to deposit: ");
                        double depAmt = scanner.nextDouble();
                        getAccount(accNum).deposit(depAmt);
                        System.out.println("✅ Deposited successfully.");
                        break;

                    case 3:
                        System.out.print("Account number: ");
                        accNum = scanner.nextLine();
                        System.out.print("Amount to withdraw: ");
                        double withAmt = scanner.nextDouble();
                        getAccount(accNum).withdraw(withAmt);
                        System.out.println("✅ Withdrawn successfully.");
                        break;

                    case 4:
                        System.out.print("Account number: ");
                        accNum = scanner.nextLine();
                        System.out.println(getAccount(accNum).getDetails());
                        break;

                    case 5:
                        running = false;
                        break;

                    default:
                        System.out.println("❌ Invalid option.");
                }
            } catch (AccountNotFoundException | InvalidTransactionException | InsufficientFundsException e) {
                System.out.println("⚠️ Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("⚠️ Unexpected error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static BankAccount getAccount(String accNum) throws AccountNotFoundException {
        BankAccount account = accounts.get(accNum);
        if (account == null)
            throw new AccountNotFoundException("Account with number " + accNum + " not found.");
        return account;
    }
}
