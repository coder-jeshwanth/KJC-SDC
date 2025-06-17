package day2.bank;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

public class BankSystem {
    public static void main(String[] args) {
        // 1. MongoDB Setup
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017"); // adjust URI if needed
        MongoDatabase database = mongoClient.getDatabase("Bank");
        MongoCollection<Document> collection = database.getCollection("Accounts");

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

                        Document existing = collection.find(eq("accountNumber", accNum)).first();
                        if (existing != null)
                            throw new InvalidTransactionException("Account already exists.");

                        Document newAccount = new Document("accountNumber", accNum)
                                .append("holderName", name)
                                .append("balance", 0.0);
                        collection.insertOne(newAccount);
                        System.out.println("✅ Account created.");
                        break;

                    case 2:
                        System.out.print("Account number: ");
                        accNum = scanner.nextLine();
                        System.out.print("Amount to deposit: ");
                        double depAmt = scanner.nextDouble();

                        Document accToDeposit = collection.find(eq("accountNumber", accNum)).first();
                        if (accToDeposit == null)
                            throw new AccountNotFoundException("Account not found.");
                        if (depAmt <= 0)
                            throw new InvalidTransactionException("Deposit amount must be positive.");

                        double newDepositBalance = accToDeposit.getDouble("balance") + depAmt;
                        collection.updateOne(eq("accountNumber", accNum),
                                new Document("$set", new Document("balance", newDepositBalance)));
                        System.out.println("✅ Deposited successfully.");
                        break;

                    case 3:
                        System.out.print("Account number: ");
                        accNum = scanner.nextLine();
                        System.out.print("Amount to withdraw: ");
                        double withAmt = scanner.nextDouble();

                        Document accToWithdraw = collection.find(eq("accountNumber", accNum)).first();
                        if (accToWithdraw == null)
                            throw new AccountNotFoundException("Account not found.");
                        double currentBalance = accToWithdraw.getDouble("balance");
                        if (withAmt <= 0)
                            throw new InvalidTransactionException("Withdraw amount must be positive.");
                        if (withAmt > currentBalance)
                            throw new InsufficientFundsException("Insufficient balance.");

                        double newWithdrawBalance = currentBalance - withAmt;
                        collection.updateOne(eq("accountNumber", accNum),
                                new Document("$set", new Document("balance", newWithdrawBalance)));
                        System.out.println("✅ Withdrawn successfully.");
                        break;

                    case 4:
                        System.out.print("Account number: ");
                        accNum = scanner.nextLine();

                        Document acc = collection.find(eq("accountNumber", accNum)).first();
                        if (acc == null)
                            throw new AccountNotFoundException("Account not found.");

                        System.out.println(acc.getString("accountNumber") + " - " +
                                acc.getString("holderName") + ": Rs. " + acc.getDouble("balance"));
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
        mongoClient.close(); // close MongoDB connection
    }
}
