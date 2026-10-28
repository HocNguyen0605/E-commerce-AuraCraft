package dto;

public class AddressDTO {
    private int id;
    private String receiver, phone, address;
    private boolean defaultAddress;

    public AddressDTO(int id, String receiver, String phone, String address, boolean defaultAddress) {
        this.id = id; this.receiver = receiver; this.phone = phone;
        this.address = address; this.defaultAddress = defaultAddress;
    }
    public int getId() { return id; }
    public String getReceiver() { return receiver; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public boolean isDefaultAddress() { return defaultAddress; }
}