package jpql.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Member {

    // 회원 PK
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    // 회원 기본 정보
    private String name;
    private int age;

    // 값 타입(임베디드) 주소
    @Embedded
    private Address address;

    // 회원(N) : 팀(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    // 회원(1) : 주문(N) 양방향 연관관계의 "읽기 전용(역방향) 컬렉션"
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    protected Member() {
    }

    // 기존 학습 코드 호환용 (age 없이 생성)
    public Member(String name, Address address, Team team) {
        this(name, 0, address, team);
    }

    // age 포함 생성자
    public Member(String name, int age, Address address, Team team) {
        this.name = name;
        this.age = age;
        this.address = address;
        changeTeam(team);
    }

    public Member(Long id, String name, int age, Address address, Team team, List<Order> orders) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.address = address;
        this.orders = orders;
        changeTeam(team);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setTeam(Team team) {
        changeTeam(team);
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public Team getTeam() {
        return team;
    }

    public List<Order> getOrders() {
        return orders;
    }

    // 양방향 연관관계 편의 메서드
    public void changeTeam(Team team) {
        if (this.team != null) {
            this.team.getMembers().remove(this);
        }
        this.team = team;
        if (team != null && !team.getMembers().contains(this)) {
            team.getMembers().add(this);
        }
    }
}
