package bank.account;

public class Account {
    private final String id;
    private final String fullName;
    private double balance;

    private final String passwordSalt;
    private final String passwordHash;

    public Account(String id, String fullName, double initialBalance, String passwordSalt, String passwordHash) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID boş olamaz");
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("Ad Soyad boş olamaz");
        if (Double.isNaN(initialBalance) || Double.isInfinite(initialBalance)) throw new IllegalArgumentException("Bakiye geçersiz");
        if (initialBalance < 0) throw new IllegalArgumentException("Başlangıç bakiyesi negatif olamaz");

        if (passwordSalt == null || passwordSalt.isBlank()) throw new IllegalArgumentException("Salt boş olamaz");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("Hash boş olamaz");

        this.id = id;
        this.fullName = fullName;
        this.balance = initialBalance;
        this.passwordSalt = passwordSalt;
        this.passwordHash = passwordHash;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public double getBalance() { return balance; }

    public String getPasswordSalt() { return passwordSalt; }
    public String getPasswordHash() { return passwordHash; }

    public void deposit(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) throw new IllegalArgumentException("Miktar geçersiz");
        if (amount <= 0) throw new IllegalArgumentException("Miktar > 0 olmalı");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) throw new IllegalArgumentException("Miktar geçersiz");
        if (amount <= 0) throw new IllegalArgumentException("Miktar > 0 olmalı");
        if (balance - amount < 0) throw new IllegalStateException("Yetersiz bakiye");
        balance -= amount;
    }
}
