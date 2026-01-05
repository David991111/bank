package bank.core;

import bank.account.Account;
import bank.transaction.Transaction;
import bank.transaction.TransactionType;
import bank.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.*;

public class Bank implements Transferable {

    private final Map<String, Account> accounts = new HashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private long nextTransactionId = 1;

    // (İstersen değiştir) faiz raporu için sabit yıllık oran
    private static final double ANNUAL_RATE = 0.05; // %5

    public void createAccount(String id, String fullName, double initialBalance, String password) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID boş olamaz");
        if (accounts.containsKey(id)) throw new IllegalArgumentException("Bu ID zaten var: " + id);
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("Ad Soyad boş olamaz");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Şifre boş olamaz");
        if (initialBalance < 0) throw new IllegalArgumentException("Başlangıç bakiyesi negatif olamaz");

        String salt = UUID.randomUUID().toString();
        String hash = SecurityUtil.hashPassword(salt, password);

        accounts.put(id, new Account(id, fullName, initialBalance, salt, hash));
    }

    public Account getAccount(String id) {
        Account a = accounts.get(id);
        if (a == null) throw new IllegalArgumentException("Hesap bulunamadı: " + id);
        return a;
    }

    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Transaction> historyFor(String accountId) {
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : transactions) {
            if (accountId.equals(t.getFromId()) || accountId.equals(t.getToId())) out.add(t);
        }
        return out;
    }

    public boolean authenticate(String accountId, String password) {
        Account a = getAccount(accountId);
        String actual = SecurityUtil.hashPassword(a.getPasswordSalt(), password);
        return a.getPasswordHash().equals(actual);
    }

    private void requireAuth(String accountId, String password) {
        if (!authenticate(accountId, password)) throw new SecurityException("Şifre yanlış!");
    }

    // sadece kendi hesabına yatırma
    public void depositToOwn(String accountId, String password, double amount) {
        requireAuth(accountId, password);
        Account a = getAccount(accountId);
        a.deposit(amount);

        transactions.add(new Transaction(nextTransactionId++, LocalDateTime.now(), TransactionType.DEPOSIT,
                null, accountId, amount, "Deposit"));
    }

    // sadece kendi hesabından çekme
    public void withdrawFromOwn(String accountId, String password, double amount) {
        requireAuth(accountId, password);
        Account a = getAccount(accountId);
        a.withdraw(amount);

        transactions.add(new Transaction(nextTransactionId++, LocalDateTime.now(), TransactionType.WITHDRAW,
                accountId, null, amount, "Withdraw"));
    }

    // transfer: gönderen giriş yapar, alıcı ID ile seçilir
    @Override
    public void transfer(String fromId, String fromPassword, String toId, double amount, String description) {
        if (fromId.equals(toId)) throw new IllegalArgumentException("Aynı hesaba transfer olmaz");
        requireAuth(fromId, fromPassword);

        Account from = getAccount(fromId);
        Account to = getAccount(toId);

        from.withdraw(amount);
        to.deposit(amount);

        transactions.add(new Transaction(nextTransactionId++, LocalDateTime.now(), TransactionType.TRANSFER,
                fromId, toId, amount, description));
    }

    // faiz raporu: id+şifre ile bakiyeye göre günlük/aylık/yıllık hesaplar
    public String interestReport(String accountId, String password) {
        requireAuth(accountId, password);
        Account a = getAccount(accountId);

        double bal = a.getBalance();
        double annual = ANNUAL_RATE;
        double monthly = annual / 12.0;
        double daily = annual / 365.0;

        double annualTl = bal * annual;
        double monthlyTl = bal * monthly;
        double dailyTl = bal * daily;

        return "Hesap: " + a.getId() +
                "\nBakiye: " + bal +
                "\nGünlük faiz: %" + (daily * 100.0) + " | TL: " + dailyTl +
                "\nAylık faiz:  %" + (monthly * 100.0) + " | TL: " + monthlyTl +
                "\nYıllık faiz: %" + (annual * 100.0) + " | TL: " + annualTl;
    }

    // CSV loader için
    public void putAccount(Account a) { accounts.put(a.getId(), a); }
    public void addTransactionDirect(Transaction t) { transactions.add(t); }
    public void setNextTransactionId(long nextId) { this.nextTransactionId = nextId; }
}
