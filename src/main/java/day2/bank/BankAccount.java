package day2.bank;

public class BankAccount {
    private String accountNumber;
    private String holderName;
    private double balance;

    public BankAccount(String accountNumber, String holderName) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = 0;
    }

    public void deposit(double amount) throws day2.bank.InvalidTransactionException {
        if (amount <= 0)
            throw new day2.bank.InvalidTransactionException("Deposit amount must be positive.");
        balance += amount;
    }

    public void withdraw(double amount) throws day2.bank.InvalidTransactionException, day2.bank.InsufficientFundsException {
        if (amount <= 0)
            throw new day2.bank.InvalidTransactionException("Withdraw amount must be positive.");
        if (amount > balance)
            throw new day2.bank.InsufficientFundsException("Insufficient balance.");
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public String getDetails() {
        return accountNumber + " - " + holderName + ": Rs. " + balance;
    }
}
