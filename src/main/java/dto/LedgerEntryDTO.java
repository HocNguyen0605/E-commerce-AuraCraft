package dto;

public class LedgerEntryDTO {
    private String code;
    private java.sql.Timestamp date;
    private String type;
    private long amount;
    private String status;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public java.sql.Timestamp getDate() { return date; }
    public void setDate(java.sql.Timestamp date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
