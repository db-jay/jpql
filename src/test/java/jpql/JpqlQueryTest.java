package jpql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jpql.domain.Order;
import jpql.domain.OrderStatus;
import jpql.dto.MemberOrderAmount;
import jpql.setup.SampleDataInitializer;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpqlQueryTest {

    private static EntityManagerFactory emf;

    private EntityManager em;
    private EntityTransaction tx;

    @BeforeAll
    static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("jpql-study");
    }

    @AfterAll
    static void tearDownFactory() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        tx = em.getTransaction();
        tx.begin();

        SampleDataInitializer.initialize(em);
        em.flush();
        em.clear();
    }

    @AfterEach
    void tearDown() {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
        if (em != null) {
            em.close();
        }
    }

    @Test
    void 기본_조건_조회() {
        List<String> names = em.createQuery(
                        "select m.name from Member m where m.address.city = :city", String.class)
                .setParameter("city", "Seoul")
                .getResultList();

        assertTrue(names.contains("Kim"));
    }

    @Test
    void 조인_집계_조회() {
        List<Object[]> rows = em.createQuery(
                        "select m.name, sum(oi.orderPrice * oi.count) " +
                                "from Order o join o.member m join o.orderItems oi " +
                                "where o.status = :status group by m.name order by m.name", Object[].class)
                .setParameter("status", OrderStatus.ORDER)
                .getResultList();

        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (Long) row[1]);
        }

        assertEquals(2, result.size());
        assertEquals(70_000L, result.get("Kim"));
        assertEquals(20_000L, result.get("Lee"));
    }

    @Test
    void fetch_join_조회() {
        List<Order> orders = em.createQuery(
                        "select distinct o from Order o " +
                                "join fetch o.member " +
                                "join fetch o.orderItems oi " +
                                "join fetch oi.item", Order.class)
                .getResultList();

        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(order -> !order.getOrderItems().isEmpty()));
    }

    @Test
    void DTO_projection_조회() {
        List<MemberOrderAmount> result = em.createQuery(
                        "select new jpql.dto.MemberOrderAmount(m.name, sum(oi.orderPrice * oi.count)) " +
                                "from Order o join o.member m join o.orderItems oi " +
                                "group by m.name order by m.name", MemberOrderAmount.class)
                .getResultList();

        assertEquals(2, result.size());
        assertEquals("Kim", result.get(0).memberName());
        assertEquals(70_000L, result.get(0).totalAmount());
        assertEquals("Lee", result.get(1).memberName());
        assertEquals(20_000L, result.get(1).totalAmount());
    }
}
