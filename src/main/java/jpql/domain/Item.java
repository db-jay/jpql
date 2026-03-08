package jpql.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Item {

    // 상품 PK
    @Id
    @GeneratedValue
    @Column(name = "ITEM_ID")
    private Long id;

    // 상품 정보
    private String name;
    private int price;
    private int stockQuantity;

    // 상품(1) : 주문상품(N)
    // mappedBy가 있으므로 연관관계의 주인은 OrderItem.item
    @OneToMany(mappedBy = "item")
    private List<OrderItem> orderItems = new ArrayList<>();

    protected Item() {
    }

    public Item(String name, int price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }
}
