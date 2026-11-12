package dto;

public class Charm {
    private int id;
    private String name;
    private long price;
    private String image;

    public Charm() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}