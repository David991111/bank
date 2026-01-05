package bank.core;

public interface Transferable {
    void transfer(String fromId, String fromPassword, String toId, double amount, String description);
}
