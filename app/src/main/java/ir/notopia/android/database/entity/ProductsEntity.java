package ir.notopia.android.database.entity;

import java.util.Date;

public class ProductsEntity {

    private String productId;
    private String productName;
    private String productImage;
    private Date dateAdd;

    public ProductsEntity(String productId, String productName, String productImage, Date dateAdd) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.dateAdd = dateAdd;
    }

    public Date getDateAdd() {
        return dateAdd;
    }

    public void setDateAdd(Date dateAdd) {
        this.dateAdd = dateAdd;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }


}
