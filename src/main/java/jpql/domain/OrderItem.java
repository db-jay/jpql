package jpql.domain;

import jakarta.persistence.*;

@Entity
public class OrderItem {

    // 주문상품 PK
    @Id
    @GeneratedValue
    @Column(name = "ORDER_ITEM_ID")
    private Long id;

    // 주문상품(N) : 주문(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID")
    private Order order;

    // 주문상품(N) : 상품(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID")
    private Item item;

    // 주문 시점 가격(상품 현재가격과 분리해서 보관)
    private int orderPrice;
    // 주문 수량
    private int count;

    protected OrderItem() {
    }

    public OrderItem(Item item, int orderPrice, int count) {
        this.item = item;
        this.orderPrice = orderPrice;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Item getItem() {
        return item;
    }

    public int getOrderPrice() {
        return orderPrice;
    }

    public int getCount() {
        return count;
    }
}
