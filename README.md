# JPQL 입문 실습 프로젝트

`/Users/sinae/git/jpaShop` 프로젝트 구성을 참고해서,
**H2 + Hibernate(JPA)** 기반으로 JPQL을 바로 테스트할 수 있게 세팅한 프로젝트입니다.

## 1) 실행 환경
- Java 21
- Maven
- H2 (Maven dependency 포함)

## 2) 핵심 설정 (`persistence.xml`)
- DB: `jdbc:h2:tcp://localhost:9092/~/git/jpql/h2db/jpql` (TCP 모드)
- 스키마 유지: `hibernate.hbm2ddl.auto=update`
- SQL 로그 최소화: `show_sql=false`, `format_sql=false`, `use_sql_comments=false`

파일: `src/main/resources/META-INF/persistence.xml`

## 3) H2 서버(TCP + 웹 콘솔) 실행
```bash
cd /Users/sinae/git/jpql
mkdir -p h2db
java -cp h2-2.2.224.jar org.h2.tools.Server -tcp -tcpPort 9092 -web -webPort 8082
```

- 웹 콘솔: `http://localhost:8082`
- JDBC URL: `jdbc:h2:tcp://localhost:9092/~/git/jpql/h2db/jpql`
- User: `sa`, Password: 빈값

## 4) 샘플 데이터 + JPQL 예제 실행
```bash
cd /Users/sinae/git/jpql
mvn -q compile exec:java -Dexec.mainClass=jpql.JpaMain
```

실행 클래스: `src/main/java/jpql/JpaMain.java`

포함된 JPQL 예제:
1. 기본 `where` 조회
2. 연관관계 `join`
3. `fetch join`
4. DTO projection + `sum` 집계

## 5) 테스트로 JPQL 연습
```bash
cd /Users/sinae/git/jpql
mvn test
```

테스트 파일: `src/test/java/jpql/JpqlQueryTest.java`

샘플 데이터 생성: `src/main/java/jpql/setup/SampleDataInitializer.java`
DTO 클래스: `src/main/java/jpql/dto/MemberOrderAmount.java`

JPQL 문자열을 직접 바꿔서 실행해보면 입문 학습에 좋습니다.
