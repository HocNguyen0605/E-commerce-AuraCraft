package dto;

public class AddressDTO {
    private int id;
    private String receiver;
    private String phone;
    private String provinceCode;
    private String province;
    private String wardCode;
    private String ward;
    private String address;        // số nhà, tên đường (với dòng cũ: chuỗi địa chỉ đầy đủ cũ)
    private boolean defaultAddress;

    public AddressDTO() { }

    public AddressDTO(int id, String receiver, String phone,
                      String provinceCode, String province, String wardCode, String ward,
                      String address, boolean defaultAddress) {
        this.id = id;
        this.receiver = receiver;
        this.phone = phone;
        this.provinceCode = provinceCode;
        this.province = province;
        this.wardCode = wardCode;
        this.ward = ward;
        this.address = address;
        this.defaultAddress = defaultAddress;
    }

    /** Giữ constructor cũ để các nơi khác trong project (nếu có) vẫn biên dịch được. */
    public AddressDTO(int id, String receiver, String phone, String address, boolean defaultAddress) {
        this(id, receiver, phone, null, null, null, null, address, defaultAddress);
    }

    /** Dùng trong JSP: ${a.fullAddress} = "số nhà, xã/phường, tỉnh/thành". */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder(address == null ? "" : address);
        if (ward != null && !ward.isBlank()) sb.append(", ").append(ward);
        if (province != null && !province.isBlank()) sb.append(", ").append(province);
        return sb.toString();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProvinceCode() { return provinceCode; }
    public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }
}