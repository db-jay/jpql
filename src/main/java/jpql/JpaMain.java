package jpql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jpql.domain.*;
import jpql.dto.MemberDTO;
import jpql.dto.MemberOrderAmount;
import jpql.setup.SampleDataInitializer;

import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        // Hibernate 기본 show_sql 출력으로 SQL을 본다. (? placeholder 유지, 컬러 하이라이트 비활성화)
        // (IntelliJ에서 실행 시 부트 로그는 최소화)
        System.setProperty("org.jboss.logging.provider", "slf4j");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        System.setProperty("org.slf4j.simpleLogger.log.org.hibernate", "error");
        System.setProperty("hibernate.show_sql", "true");
        System.setProperty("hibernate.format_sql", "true");
        System.setProperty("hibernate.highlight_sql", "false");

        // persistence.xml의 persistence-unit name("jpql-study")과 반드시 같아야 한다.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpql-study");
        // EntityManager: 1회성 DB 작업 단위(보통 트랜잭션 단위로 생성/종료)
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        // JPA에서 CUD/flush는 트랜잭션 안에서 동작한다.
        tx.begin();

        try {
            // 예제용 데이터 적재 (이미 있으면 skip)
            SampleDataInitializer.initialize(em);

            // SQL 즉시 반영 + 영속성 컨텍스트 초기화(조회 예제에서 DB 기준으로 보기 위함)
            em.flush();
            em.clear();

            // ===== Projection 학습 (한 번에 하나씩 주석 해제해서 실행) =====
//            memberEntityProjection(em);      // SELECT m FROM Member m
//            teamEntityProjection(em);        // SELECT m.team FROM Member m
//            memberAddressProjection(em);     // SELECT m.address FROM Member m
//            memberScalarProjection(em);        // SELECT m.name, m.age FROM Member m

            // 페이징
            pagingQuery(em, 0, 10);

            // 서브쿼리
            subQueryExamples(em);

            // 조인
            // 1. INNER JOIN
//            String query = "SELECT m FROM Member m INNER JOIN m.team t";
//            List<Member> members = em.createQuery(query, Member.class)
//                    .setFirstResult(1)
//                    .setMaxResults(10)
//                    .getResultList();

            // 2. LEFT OUTER JOIN
            String query = "SELECT m, t FROM Member m LEFT JOIN m.team t";
            List<Object[]> rows = em.createQuery(query, Object[].class).getResultList();


            // Bulk delete 연습 시 FK 제약 순서를 지켜야 한다.
            // 잘못된 예: delete from Order 먼저 실행 -> OrderItem FK 위반 발생
            // em.createQuery("delete from OrderItem oi").executeUpdate();
            // em.createQuery("delete from Order o").executeUpdate();

            // 엔티티 자체를 조회하는 가장 기본적인 JPQL 형태
//            basicWhereQuery(em, "Seoul");

            // 연관관계를 따라 조인하는 JPQL 예제
//            joinQuery(em);

            // fetch join: 연관 엔티티를 한 번의 SQL로 같이 조회 (N+1 문제 학습 포인트)
//            fetchJoinQuery(em);

            // select new: 조회 결과를 DTO로 바로 매핑
//            dtoProjectionWithAggregateQuery(em);


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            // 리소스는 사용 후 반드시 닫아준다.
            em.close();
            emf.close();
        }
    }

    private static void basicWhereQuery(EntityManager em, String city) {
        // 엔티티 자체를 조회하는 가장 기본적인 JPQL 형태
        // :city 는 named parameter, setParameter로 값 바인딩
        List<Member> members = em.createQuery(
                        "select m from Member m where m.address.city = :city order by m.name", Member.class)
                .setParameter("city", city)
                .getResultList();

//        System.out.println("\n=== [JPQL #1] 기본 where 조회 ===");
        members.forEach(m -> System.out.printf("member=%s, city=%s%n", m.getName(), m.getAddress().getCity()));
    }

    private static void memberEntityProjection(EntityManager em) {
        // 1) 엔티티 프로젝션: SELECT m FROM Member m
        List<Member> resultList = em.createQuery("select m from Member m order by m.id", Member.class).getResultList();

        System.out.println("\n=== [Projection #1] SELECT m FROM Member m ===");
        resultList.forEach(m -> System.out.printf("memberId=%s, name=%s, team=%s%n",
                m.getId(), m.getName(), m.getTeam().getName()));
    }

    private static void teamEntityProjection(EntityManager em) {
        // 2) 엔티티 프로젝션: SELECT m.team FROM Member m
        //    연관 엔티티(Team) 자체를 조회한다.
        List<Team> teams = em.createQuery(
                        "select m.team from Member m order by m.id", Team.class)
                .getResultList();

        System.out.println("\n=== [Projection #2] SELECT m.team FROM Member m ===");
        teams.forEach(team -> System.out.printf("teamId=%s, teamName=%s%n",
                team.getId(), team.getName()));

        // 중복 제거 버전
        List<Team> distinctTeams = em.createQuery(
                        "select distinct m.team from Member m", Team.class)
                .getResultList();
        System.out.printf("distinct team count = %d%n", distinctTeams.size());
    }

    private static void memberAddressProjection(EntityManager em) {
        // 3) 임베디드 타입 프로젝션: SELECT m.address FROM Member m
        List<Address> addresses = em.createQuery(
                        "select m.address from Member m order by m.id", Address.class)
                .getResultList();

        System.out.println("\n=== [Projection #3] SELECT m.address FROM Member m ===");
        addresses.forEach(a -> System.out.printf("city=%s, street=%s, zipcode=%s%n",
                a.getCity(), a.getStreet(), a.getZipcode()));
    }

    private static void memberScalarProjection(EntityManager em) {
        // 4) 스칼라 타입 프로젝션(여러 값 조회): SELECT m.name, m.age FROM Member m
        //    여러 스칼라 값을 DTO로 바로 매핑한다.
        List<MemberDTO> result = em.createQuery(
                        "select new jpql.dto.MemberDTO(m.name, m.age) from Member m order by m.id",
                        MemberDTO.class)
                .getResultList();

        System.out.println("\n=== [Projection #4] SELECT m.name, m.age FROM Member m ===");
        result.forEach(dto -> System.out.printf("name=%s, age=%s%n", dto.getName(), dto.getAge()));
    }

    private static void pagingQuery(EntityManager em, int firstResult, int maxResults) {
        List<Member> result = em.createQuery(
                        "select m from Member m order by m.age desc", Member.class)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList();

        System.out.println("result.size = " + result.size());
        for (Member member : result) {
            System.out.println("member1" + member);
        }
    }

    private static void subQueryExamples(EntityManager em) {
        // JPQL 서브쿼리 예제 #1: 평균 나이 이상 회원 (비연관 서브쿼리)
        List<Member> avgOrOlderMembers = em.createQuery(
                        "select m from Member m " +
                                "where m.age >= (select avg(m2.age) from Member m2) " +
                                "order by m.id", Member.class)
                .getResultList();

        System.out.println("\n=== [SubQuery #1] 평균 나이 이상 회원 ===");
        avgOrOlderMembers.forEach(m ->
                System.out.printf("member=%s, age=%d%n", m.getName(), m.getAge()));

        // JPQL 서브쿼리 예제 #2: 주문이 있는 회원 (연관 서브쿼리 + EXISTS)
        List<Member> orderedMembers = em.createQuery(
                        "select m from Member m " +
                                "where exists (" +
                                "  select o.id from Order o " +
                                "  where o.member = m and o.status = :status" +
                                ") " +
                                "order by m.id", Member.class)
                .setParameter("status", OrderStatus.ORDER)
                .getResultList();

        System.out.println("\n=== [SubQuery #2] 주문이 있는 회원(EXISTS) ===");
        orderedMembers.forEach(m -> System.out.printf("member=%s%n", m.getName()));
    }


    private static void joinQuery(EntityManager em) {
        // 연관관계를 따라 조인하는 JPQL 예제
        // 결과 타입을 Object[]로 받으면 "컬럼 묶음"처럼 사용할 수 있다.
        List<Object[]> rows = em.createQuery(
                        "select o.id, m.name, oi.item.name, oi.count " +
                                "from Order o join o.member m join o.orderItems oi order by o.id", Object[].class)
                .getResultList();

        System.out.println("\n=== [JPQL #2] 조인 조회 ===");
        rows.forEach(row -> System.out.printf("orderId=%s, member=%s, item=%s, count=%s%n",
                row[0], row[1], row[2], row[3]));
    }

    private static void fetchJoinQuery(EntityManager em) {
        // fetch join: 연관 엔티티를 한 번의 SQL로 같이 조회 (N+1 문제 학습 포인트)
        // 컬렉션 fetch join 시 중복 row가 생길 수 있어 distinct를 자주 사용한다.
        List<Order> orders = em.createQuery(
                        "select distinct o from Order o " +
                                "join fetch o.member " +
                                "join fetch o.orderItems oi " +
                                "join fetch oi.item " +
                                "order by o.id", Order.class)
                .getResultList();

        System.out.println("\n=== [JPQL #3] fetch join ===");
        orders.forEach(order -> System.out.printf("orderId=%s, member=%s, orderItems=%s%n",
                order.getId(), order.getMember().getName(), order.getOrderItems().size()));
    }

    private static void dtoProjectionWithAggregateQuery(EntityManager em) {
        // select new: 조회 결과를 DTO로 바로 매핑
        // group by + sum 집계 예제
        List<MemberOrderAmount> stats = em.createQuery(
                        "select new jpql.dto.MemberOrderAmount(m.name, sum(oi.orderPrice * oi.count)) " +
                                "from Order o join o.member m join o.orderItems oi " +
                                "where o.status = :status " +
                                "group by m.name order by m.name", MemberOrderAmount.class)
                .setParameter("status", OrderStatus.ORDER)
                .getResultList();

        System.out.println("\n=== [JPQL #4] DTO projection + 집계 조회 ===");
        stats.forEach(System.out::println);
    }
}
