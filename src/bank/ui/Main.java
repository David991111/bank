package bank.ui;

import bank.core.Bank;
import bank.persistence.BankCsvStorage;
import bank.transaction.Transaction;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static String readNonBlank(Scanner sc, String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            if (!s.isBlank()) return s;
            System.out.println("HATA: Boş olamaz.");
        }
    }

    private static double readNonNegativeDouble(Scanner sc, String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(s);
                if (v >= 0) return v;
                System.out.println("HATA: Negatif olamaz.");
            } catch (Exception e) {
                System.out.println("HATA: Sayı gir.");
            }
        }
    }

    private static double readPositiveDouble(Scanner sc, String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(s);
                if (v > 0) return v;
                System.out.println("HATA: Değer > 0 olmalı.");
            } catch (Exception e) {
                System.out.println("HATA: Sayı gir.");
            }
        }
    }

    public static void main(String[] args) {
        Bank bank = new Bank();
        BankCsvStorage storage = new BankCsvStorage();

        try { storage.loadInto(bank); }
        catch (Exception e) { System.out.println("CSV yüklenemedi (devam): " + e.getMessage()); }

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== BANK MENU ===");
            System.out.println("1) Hesapları listele (ID + Ad Soyad)");
            System.out.println("2) Hesap aç (ID + Ad Soyad + Şifre + Başlangıç bakiye)");
            System.out.println("3) Para yatır (kendi hesabına) [ID+Şifre]");
            System.out.println("4) Para çek (kendi hesabından) [ID+Şifre]");
            System.out.println("5) Transfer (giriş -> listeden seç -> ID ile gönder)");
            System.out.println("6) İşlem geçmişi (ID)");
            System.out.println("7) Faiz raporu (ID+Şifre)");
            System.out.println("9) Kaydet & Çık");
            System.out.print("Seçim: ");

            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> {
                        System.out.println("--- Accounts ---");
                        for (var a : bank.getAllAccounts()) {
                            System.out.println(a.getId() + " | " + a.getFullName());
                        }
                    }
                    case "2" -> {
                        String id = readNonBlank(sc, "ID (sen belirle): ");
                        String name = readNonBlank(sc, "Ad Soyad: ");
                        String pass = readNonBlank(sc, "Şifre: ");
                        double bal = readNonNegativeDouble(sc, "Başlangıç bakiye: ");
                        bank.createAccount(id, name, bal, pass);
                        System.out.println("OK: Hesap oluşturuldu.");
                    }
                    case "3" -> {
                        String id = readNonBlank(sc, "ID: ");
                        String pass = readNonBlank(sc, "Şifre: ");
                        double amount = readPositiveDouble(sc, "Yatırılacak miktar: ");
                        bank.depositToOwn(id, pass, amount);
                        System.out.println("OK: Yatırıldı.");
                    }
                    case "4" -> {
                        String id = readNonBlank(sc, "ID: ");
                        String pass = readNonBlank(sc, "Şifre: ");
                        double amount = readPositiveDouble(sc, "Çekilecek miktar: ");
                        bank.withdrawFromOwn(id, pass, amount);
                        System.out.println("OK: Çekildi.");
                    }
                    case "5" -> {
                        System.out.println("Gönderen hesap giriş:");
                        String fromId = readNonBlank(sc, "ID: ");
                        String pass = readNonBlank(sc, "Şifre: ");

                        System.out.println("\n--- Hesap listesi (ID + Ad Soyad) ---");
                        for (var a : bank.getAllAccounts()) {
                            System.out.println(a.getId() + " | " + a.getFullName());
                        }

                        String toId = readNonBlank(sc, "Alıcı ID: ");
                        double amount = readPositiveDouble(sc, "Gönderilecek miktar: ");
                        bank.transfer(fromId, pass, toId, amount, "Transfer");
                        System.out.println("OK: Transfer yapıldı.");
                    }
                    case "6" -> {
                        String id = readNonBlank(sc, "Hesap ID: ");
                        List<Transaction> h = bank.historyFor(id);
                        System.out.println("--- History (" + id + ") ---");
                        for (Transaction t : h) System.out.println(t);
                    }
                    case "7" -> {
                        String id = readNonBlank(sc, "ID: ");
                        String pass = readNonBlank(sc, "Şifre: ");
                        System.out.println("\n=== FAİZ RAPORU ===");
                        System.out.println(bank.interestReport(id, pass));
                    }
                    case "9" -> {
                        storage.saveFrom(bank);
                        System.out.println("Kaydedildi. Çıkış.");
                        return;
                    }
                    default -> System.out.println("Geçersiz seçim.");
                }
            } catch (Exception e) {
                System.out.println("HATA: " + e.getMessage());
            }
        }
    }
}
