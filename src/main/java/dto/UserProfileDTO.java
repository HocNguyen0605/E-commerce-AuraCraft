package dto;

import java.sql.Date;

public class UserProfileDTO {
    private int id;
    private String name, email, phone;
    private Date birthday;

    public UserProfileDTO(int id, String name, String email, String phone, Date birthday) {
        this.id = id; this.name = name; this.email = email; this.phone = phone; this.birthday = birthday;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Date getBirthday() { return birthday; }
}