package bank.persistence;

import bank.account.Account;
import bank.core.Bank;
import bank.transaction.Transaction;
import bank.transaction.TransactionType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

public class BankCsvStorage {
    private final Path accountsPath = Paths.get("data", "accounts.csv");
    private final Path transactionsPath = Paths.get("data", "transactions.csv");

    public void loadInto(Bank bank) {
        loadAccounts(bank);
        loadTransactions(bank);
    }

    public void saveFrom(Bank bank) {
        saveAccounts(bank);
        saveTransactions(bank);
    }

    private void ensureDataDir() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            throw new RuntimeException("data/ klasörü oluşturulamadı", e);
        }
    }

    private void loadAccounts(Bank bank) {
        if (!Files.exists(accountsPath)) return;

        try (BufferedReader br = Files.newBufferedReader(accountsPath)) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> f = CsvUtil.split(line);

                // id,fullName,balance,passwordSalt,passwordHash
                String id = f.get(0);
                String fullName = f.get(1);
                double balance = Double.parseDouble(f.get(2));
                String salt = f.get(3);
                String hash = f.get(4);

                bank.putAccount(new Account(id, fullName, balance, salt, hash));
            }
        } catch (IOException e) {
            throw new RuntimeException("accounts.csv okunamadı", e);
        }
    }

    private void loadTransactions(Bank bank) {
        if (!Files.exists(transactionsPath)) return;

        long maxId = 0;
        try (BufferedReader br = Files.newBufferedReader(transactionsPath)) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> f = CsvUtil.split(line);

                long id = Long.parseLong(f.get(0));
                LocalDateTime ts = LocalDateTime.parse(f.get(1));
                TransactionType type = TransactionType.valueOf(f.get(2));
                String fromId = f.get(3).isBlank() ? null : f.get(3);
                String toId = f.get(4).isBlank() ? null : f.get(4);
                double amount = Double.parseDouble(f.get(5));
                String desc = f.size() >= 7 ? f.get(6) : "";

                bank.addTransactionDirect(new Transaction(id, ts, type, fromId, toId, amount, desc));
                if (id > maxId) maxId = id;
            }
        } catch (IOException e) {
            throw new RuntimeException("transactions.csv okunamadı", e);
        }

        bank.setNextTransactionId(maxId + 1);
    }

    private void saveAccounts(Bank bank) {
        ensureDataDir();
        try (BufferedWriter bw = Files.newBufferedWriter(accountsPath)) {
            bw.write("id,fullName,balance,passwordSalt,passwordHash\n");

            for (Account a : bank.getAllAccounts()) {
                bw.write(
                        CsvUtil.escape(a.getId()) + "," +
                                CsvUtil.escape(a.getFullName()) + "," +
                                a.getBalance() + "," +
                                CsvUtil.escape(a.getPasswordSalt()) + "," +
                                CsvUtil.escape(a.getPasswordHash()) + "\n"
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("accounts.csv yazılamadı", e);
        }
    }

    private void saveTransactions(Bank bank) {
        ensureDataDir();
        try (BufferedWriter bw = Files.newBufferedWriter(transactionsPath)) {
            bw.write("id,timestamp,type,fromId,toId,amount,description\n");

            for (Transaction t : bank.getAllTransactions()) {
                String from = t.getFromId() == null ? "" : CsvUtil.escape(t.getFromId());
                String to = t.getToId() == null ? "" : CsvUtil.escape(t.getToId());
                String desc = CsvUtil.escape(t.getDescription());

                bw.write(t.getId() + "," + t.getTimestamp() + "," + t.getType() + "," +
                        from + "," + to + "," + t.getAmount() + "," + desc + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("transactions.csv yazılamadı", e);
        }
    }
}
