package dto;

import java.sql.Timestamp;

public class Withdrawal {
    private int id;
    private int shopId;
    private long amount;
    private String bankName;
    private String bankAccount;
    private String accountHolder;
    private String status;
    private Timestamp dateCreate;

    public Withdrawal() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getDateCreate() { return dateCreate; }
    public void setDateCreate(Timestamp dateCreate) { this.dateCreate = dateCreate; }
}