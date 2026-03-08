package jpql.setup;

import jakarta.persistence.EntityManager;
import jpql.domain.*;

import java.time.LocalDateTime;

public final class SampleDataInitializer {

    private SampleDataInitializer() {
    }

    public static void initialize(EntityManager em) {
        Long count = em.createQuery("select count(m) from Member m", Long.class)
                .getSingleResult();

        if (count > 0) {
            return;
        }

        Team backendTeam = new Team("Backend Team");
        Team dataTeam = new Team("Data Team");
        em.persist(backendTeam);
        em.persist(dataTeam);

        // JPQL 프로젝션 학습용 dummy members
        Member kim = new Member("Kim", 20, new Address("Seoul",  "Gangnam-daero", "06236"), backendTeam);
        Member lee = new Member("Lee", 20, new Address("Busan", "Centum-ro", "48058"), dataTeam);
        Member park = new Member("Park", 20, new Address("Seoul", "Mapo-daero", "04168"), backendTeam);

        em.persist(kim);
        em.persist(lee);
        em.persist(park);

        Item jpaBook = new Item("JPA Book", 30_000, 100);
        Item sqlBook = new Item("SQL Book", 20_000, 100);

        em.persist(jpaBook);
        em.persist(sqlBook);

        Order order1 = new Order(
                kim,
                LocalDateTime.now().minusDays(1),
                OrderStatus.ORDER,
                new Address("Seoul", "Teheran-ro", "06134")
        );
        order1.addOrderItem(new OrderItem(jpaBook, jpaBook.getPrice(), 1));
        order1.addOrderItem(new OrderItem(sqlBook, sqlBook.getPrice(), 2));

        Order order2 = new Order(
                lee,
                LocalDateTime.now(),
                OrderStatus.ORDER,
                new Address("Busan", "Marine city", "48092")
        );
        order2.addOrderItem(new OrderItem(sqlBook, sqlBook.getPrice(), 1));

        em.persist(order1);
        em.persist(order2);
    }
}
