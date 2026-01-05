package bank.transaction;

import java.time.LocalDateTime;

public class Transaction {
    private final long id;
    private final LocalDateTime timestamp;
    private final TransactionType type;
    private final String fromId;
    private final String toId;
    private final double amount;
    private final String description;

    public Transaction(long id, LocalDateTime timestamp, TransactionType type,
                       String fromId, String toId, double amount, String description) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
        this.description = description == null ? "" : description;
    }

    public long getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public TransactionType getType() { return type; }
    public String getFromId() { return fromId; }
    public String getToId() { return toId; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "#" + id + " | " + timestamp + " | " + type +
                " | from=" + fromId + " to=" + toId +
                " | amount=" + amount + " | " + description;
    }
}
