# JPA / Hibernate — Покроковий План Впровадження

> **Контекст**: Cafe Order Service, Spring Boot 3, PostgreSQL, Flyway.
> Цей план — інструкція для самостійної реалізації.

---

## Крок 0: Залежності (`pom.xml`)

### Що зробити:
1. Додати `<parent>` — Spring Boot Starter Parent (версія `3.3.1`)
2. Змінити `<properties>`: встановити `<java.version>17</java.version>`
3. Додати залежності:
   - `spring-boot-starter-data-jpa` — JPA + Hibernate + HikariCP
   - `postgresql` (scope: `runtime`) — JDBC-драйвер PostgreSQL
   - `flyway-core` + `flyway-database-postgresql` — міграції
   - `lombok` (optional: true) — зменшення boilerplate
   - `spring-boot-starter-test` (scope: test)
   - `spring-boot-testcontainers` (scope: test)
   - `testcontainers:postgresql` (scope: test)
   - `testcontainers:junit-jupiter` (scope: test)
4. Замінити `maven-jar-plugin` на `spring-boot-maven-plugin` (виключити lombok)
5. Видалити ручне управління версіями JUnit — Spring Boot BOM робить це автоматично

### Чому `starter-parent`?
Він керує версіями ВСІХ транзитивних залежностей. Тобі не потрібно вказувати версії для PostgreSQL, Flyway, Lombok — тільки для parent.

---

## Крок 1: Конфігурація (`application.yml`)

### Що зробити:
Створити файл `src/main/resources/application.yml`

### Що має містити:

#### 1.1 Datasource + HikariCP
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cafe_db
    username: ${DB_USERNAME:cafe_user}
    password: ${DB_PASSWORD:cafe_pass}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 30000
      max-lifetime: 1800000
      connection-timeout: 5000
```
- `${DB_USERNAME:cafe_user}` — бере з env-змінної, fallback на `cafe_user`
- HikariCP вже включений у starter — тільки налаштуй параметри пулу

#### 1.2 JPA / Hibernate
```yaml
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
    show-sql: false
```

| Параметр | Значення | Пояснення |
|---|---|---|
| `ddl-auto` | `validate` | **НІКОЛИ** не `update`/`create` в production. Hibernate лише перевіряє що entity відповідають схемі. Flyway керує міграціями |
| `open-in-view` | `false` | Вимикає антипатерн OSIV. Lazy loading поза транзакцією кине `LazyInitializationException` — це ДОБРЕ, бо змусить тебе правильно проєктувати запити |
| `show-sql` | `false` | Використовуй logging замість цього |

#### 1.3 Flyway
```yaml
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

#### 1.4 Логування SQL (тільки для dev)
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
```

#### 1.5 Docker Compose для PostgreSQL
Створи `docker-compose.yml` в корені проєкту з сервісом `postgres:16-alpine`, портом `5432`, та credentials що відповідають `application.yml`.

---

## Крок 2: BaseEntity — базовий клас сутностей

### Що зробити:
Створити `org.example.orderservice.domain.base.BaseEntity`

### Що має містити:
- Анотація `@MappedSuperclass`
- Анотація `@EntityListeners(AuditingEntityListener.class)`
- Поле `Long id` з `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Поле `Instant createdAt` з `@CreatedDate`, `@Column(updatable = false)`
- Поле `Instant lastModifiedAt` з `@LastModifiedDate`
- Правильний `equals()` — порівнює тільки по `id`, перевіряє `id != null`
- Правильний `hashCode()` — повертає `getClass().hashCode()` (константа!)

### Чому `IDENTITY` для генерації ID?
- Маппиться на PostgreSQL `BIGSERIAL` — нативно і ефективно
- Для монолітного додатку це найпростіший вибір
- `SEQUENCE` теж гарний, але потребує додаткової конфігурації
- `UUID` повільніший для B-tree індексів, зайвий тут

### Чому `hashCode()` повертає константу?
JPA entity проходить lifecycle: `new` (id=null) → `managed` (id=1) → `detached`. Якщо hashCode залежить від id, то `HashSet` і `HashMap` зламаються після `persist()`, бо hash-код зміниться.

### Увімкнути аудит:
Створи `org.example.orderservice.config.JpaConfig` з анотаціями `@Configuration` + `@EnableJpaAuditing`

---

## Крок 3: Проєктування сутностей (Entities)

### 3.0 Нова пакетна структура
```
org.example.orderservice/
├── OrderServiceApplication.java     ← @SpringBootApplication
├── config/
│   └── JpaConfig.java               ← @EnableJpaAuditing
├── domain/
│   ├── base/BaseEntity.java
│   ├── ingredient/IngredientEntity.java
│   ├── item/ItemEntity.java
│   ├── order/
│   │   ├── OrderEntity.java
│   │   ├── OrderItemEntity.java
│   │   └── OrderStatus.java         ← enum
│   └── payment/PaymentEntity.java
├── repository/                       ← Spring Data JPA інтерфейси
├── service/                          ← бізнес-логіка з @Transactional
└── cli/                              ← CommandLineRunner (твій поточний CLI)
```

### 3.1 Lombok та JPA — правила безпеки

| Анотація | Безпечно? | Пояснення |
|---|---|---|
| `@Getter` | ✅ Так | Без побічних ефектів |
| `@Setter` | ⚠️ Точково | Тільки на окремих полях, не на класі |
| `@NoArgsConstructor(PROTECTED)` | ✅ Так | JPA вимагає no-arg конструктор |
| `@ToString(exclude = "...")` | ✅ Так | **Обов'язково** виключай lazy-колекції! |
| `@Data` | ❌ **НІКОЛИ** | Генерує equals/hashCode по всіх полях — ламає JPA |
| `@EqualsAndHashCode` | ❌ **НІКОЛИ** | Те саме — equals/hashCode мають бути в BaseEntity |

### 3.2 IngredientEntity
- `extends BaseEntity`
- Поля: `String type`, `String name` (unique), `int stockCount`
- Бізнес-метод: `deductStock(int amount)` з перевіркою достатності
- Таблиця: `ingredients`

### 3.3 ItemEntity
- `extends BaseEntity`
- Поля: `String type`, `String name` (unique), `BigDecimal price`
- Зв'язок: `@ManyToMany(fetch = LAZY)` з `IngredientEntity` через join-таблицю `item_ingredients`
- Використовуй `@JoinTable` з `joinColumns` / `inverseJoinColumns`
- Таблиця: `items`

### 3.4 OrderStatus (enum)
- Значення: `CREATED`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

### 3.5 OrderEntity
- `extends BaseEntity`
- Поля: `OrderStatus status` з `@Enumerated(EnumType.STRING)` ← **не ORDINAL!**
- Зв'язок: `@OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true)` до `OrderItemEntity`
- Зв'язок: `@OneToOne(mappedBy = "order", cascade = ALL)` до `PaymentEntity`
- **Обов'язково**: helper-метод `addItem(OrderItemEntity)` який синхронізує обидві сторони зв'язку:
  ```java
  public void addItem(OrderItemEntity item) {
      items.add(item);
      item.setOrder(this);  // ← синхронізація!
  }
  ```
- `getItems()` має повертати `Collections.unmodifiableList(items)`
- Таблиця: `orders`

### 3.6 OrderItemEntity
- `extends BaseEntity`
- Зв'язок: `@ManyToOne(fetch = LAZY)` до `OrderEntity`
- Зв'язок: `@ManyToOne(fetch = LAZY)` до `ItemEntity`
- Поле: `int quantity`
- **Обидва `@ManyToOne` мають бути `LAZY`** — це ключ до уникнення N+1
- Таблиця: `order_items`

### 3.7 PaymentEntity
- `extends BaseEntity`
- Зв'язок: `@OneToOne(fetch = LAZY)` до `OrderEntity` (з `@JoinColumn(unique = true)`)
- Поля: `BigDecimal amount`, `String method`
- Таблиця: `payments`

---

## Крок 4: Flyway міграція

### Що зробити:
Створити файл `src/main/resources/db/migration/V1__init_schema.sql`

### Конвенція іменування:
```
V{version}__{опис}.sql    ← ДВА підкреслення!

V1__init_schema.sql
V2__add_order_notes.sql
V3__seed_ingredients.sql
```

### Що має містити V1:
- `CREATE TABLE ingredients` — id BIGSERIAL PK, type, name (UNIQUE), stock_count, created_at, last_modified_at
- `CREATE TABLE items` — id BIGSERIAL PK, type, name (UNIQUE), price NUMERIC(10,2), audit fields
- `CREATE TABLE item_ingredients` — composite PK (item_id, ingredient_id), required_count, FK до items та ingredients
- `CREATE TABLE orders` — id BIGSERIAL PK, status VARCHAR(50) DEFAULT 'CREATED', audit fields
- `CREATE TABLE order_items` — id BIGSERIAL PK, order_id FK, item_id FK, quantity, audit fields
- `CREATE TABLE payments` — id BIGSERIAL PK, order_id FK (UNIQUE), amount, method, audit fields
- Індекси: `idx_order_items_order_id`, `idx_orders_status`

### Порада:
Використовуй `TIMESTAMP NOT NULL DEFAULT now()` для `created_at` — це backup на рівні БД, навіть якщо JPA auditing не спрацює.

---

## Крок 5: Репозиторії та транзакції

### 5.1 Репозиторії
Створи інтерфейси що extends `JpaRepository<Entity, Long>`:

- **`OrderRepository`**:
  - `List<OrderEntity> findByStatus(OrderStatus status)`
  - Метод з `@EntityGraph(attributePaths = {"items", "items.item"})` для завантаження замовлення з items одним запитом

- **`ItemRepository`**:
  - `Optional<ItemEntity> findByName(String name)`
  - Метод з `@EntityGraph(attributePaths = {"ingredients"})` для завантаження item з інгредієнтами

- **`IngredientRepository`**:
  - `Optional<IngredientEntity> findByName(String name)`

- **`PaymentRepository`**: базовий, без кастомних методів

### 5.2 Правила `@Transactional`

**Де ставити:**

| Місце | Рекомендація |
|---|---|
| **Service-клас** | ✅ `@Transactional(readOnly = true)` на рівні класу |
| **Write-методи** | ✅ `@Transactional` (override, readOnly = false) на методі |
| Repository | ❌ Spring Data вже обгортає кожен метод |
| Controller / CLI | ❌ Транзакція не має жити протягом всього запиту |

**Патерн:**
```java
@Service
@Transactional(readOnly = true)   // default для всього класу
public class OrderService {

    public OrderEntity getOrder(Long id) { ... }  // read-only

    @Transactional  // override → readOnly = false
    public OrderEntity createOrder(...) { ... }    // write
}
```

**Чому `readOnly = true` за замовчуванням?**
Hibernate вимикає dirty-checking для read-only транзакцій → приріст продуктивності.

---

## Крок 6: Оптимізація — проблема N+1

### Що це:
```
SELECT * FROM orders;                    -- 1 запит
SELECT * FROM order_items WHERE order_id = 1;  -- +1
SELECT * FROM order_items WHERE order_id = 2;  -- +1
SELECT * FROM order_items WHERE order_id = 3;  -- +1
-- ... N додаткових запитів
```

### Як вирішити:

**Варіант 1: `@EntityGraph`** (декларативно)
```java
@EntityGraph(attributePaths = {"items", "items.item"})
@Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
Optional<OrderEntity> findByIdWithItems(@Param("id") Long id);
```

**Варіант 2: `JOIN FETCH`** (в JPQL)
```java
@Query("SELECT o FROM OrderEntity o " +
       "LEFT JOIN FETCH o.items oi " +
       "LEFT JOIN FETCH oi.item " +
       "WHERE o.id = :id")
Optional<OrderEntity> findByIdWithDetails(@Param("id") Long id);
```

**Правило**: Якщо тобі потрібні дані з вкладених колекцій — завжди використовуй `@EntityGraph` або `JOIN FETCH`. Ніколи не покладайся на lazy loading у циклі.

---

## Крок 7: Тестування з Testcontainers

### Що зробити:
Створити інтеграційний тест що піднімає реальний PostgreSQL в Docker-контейнері.

### Ключові анотації:
- `@SpringBootTest` — піднімає весь Spring context
- `@Testcontainers` — керує lifecycle контейнерів
- `@Container` + `@ServiceConnection` — Spring Boot 3.1+ автоматично підставляє URL/username/password

### Структура тесту:
1. Оголоси `static PostgreSQLContainer<?>` з `@Container` + `@ServiceConnection`
2. `@Autowired` потрібний репозиторій
3. Напиши тест: створи entity → `save()` → перевір що `id` не null, `createdAt` не null

### Перевага `@ServiceConnection`:
Не потрібно писати `@DynamicPropertySource` вручну — Spring Boot автоматично конфігурує datasource з контейнера.

---

## Крок 8: Spring Boot Application

### Що зробити:
1. Створити `org.example.orderservice.OrderServiceApplication` з `@SpringBootApplication` і `main()`
2. Поточний CLI адаптувати через `CommandLineRunner` — Spring Bean що запускається при старті
3. Старий `org.Main` можна видалити

---

## Чек-лист перевірки

- [ ] `docker compose up -d` — PostgreSQL працює
- [ ] `mvn clean compile` — проєкт збирається
- [ ] `mvn spring-boot:run` — Flyway створює таблиці, Hibernate validate проходить
- [ ] SQL-клієнтом перевірити що всі таблиці та індекси створені
- [ ] Testcontainers тест проходить: `mvn test`
