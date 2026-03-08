package jpql.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDERS")
public class Order {

    // 주문 PK
    @Id
    @GeneratedValue
    @Column(name = "ORDER_ID")
    private Long id;

    // 주문(N) : 회원(1), 지연로딩으로 필요할 때 회원 조회
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    // 주문(1) : 주문상품(N)
    // cascade=ALL -> Order 저장/삭제 시 OrderItem도 함께 전파
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;

    // Enum은 문자열로 저장해야(ORDINAL 지양) 값 변경에 안전
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // 주문 시점 배송지 값 타입(임베디드 타입)
    @Embedded
    private Address address;

    protected Order() {
    }

    public Order(Member member, LocalDateTime orderDate, OrderStatus status) {
        this(member, orderDate, status, null);
    }

    public Order(Member member, LocalDateTime orderDate, OrderStatus status, Address address) {
        changeMember(member);
        this.orderDate = orderDate;
        this.status = status;
        this.address = address;
    }

    // 양방향 연관관계 편의 메서드: 양쪽 객체를 함께 맞춰준다.
    public void changeMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    // 주문-주문상품 양방향 연관관계 편의 메서드
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public Address getAddress() {
        return address;
    }
}
