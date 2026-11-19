package dto;

public class Category {
    private int id;
    private String name;
    private int productCount;
    private boolean base;   // true = danh mục "Dây nền" (categories.is_base = 1)

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isBase() { return base; }
    public void setBase(boolean base) { this.base = base; }
    public int getProductCount() { return productCount; }
    public void setProductCount(int productCount) { this.productCount = productCount; }
}