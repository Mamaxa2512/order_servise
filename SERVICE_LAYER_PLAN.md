# План імплементації: Сервісний шар (Service Layer)

## Поточний стан проекту

На даний момент у нас готові:

- **Domain Entities**: `BaseEntity`, `ItemEntity`, `IngredientEntity`, `OrderEntity`, `OrderItemEntity`, `PaymentEntity`, `OrderStatus`
- **Repositories**: `ItemRepository`, `IngredientRepository`, `OrderRepository`, `PaymentRepository`
- **DTOs**: Request/Response класи для всіх ресурсів
- **MapStruct Mappers**: `ItemMapper`, `IngredientMapper`, `OrderMapper`, `PaymentMapper`
- **Database**: Flyway міграції (`V1__init_schema.sql`, `V2__seed_data.sql`), PostgreSQL

**Мета**: Створити сервісний шар, який з'єднає репозиторії, маппери та бізнес-логіку, підготувавши проект до REST API.

---

## Фаза 1. Custom Exceptions + Global Error Handling

Перш ніж писати сервіси, потрібно підготувати систему обробки помилок.

### 1.1 `ResourceNotFoundException`

- **Пакет**: `org.example.orderservice.exception`
- **Extends**: `RuntimeException`
- **Конструктор**: приймає `String resourceName` та `Long id`
- **Message**: формат `"Item with id 42 not found"`
- **Призначення**: кидається коли ресурс не знайдено за ID

### 1.2 `OrderStatusException`

- **Пакет**: `org.example.orderservice.exception`
- **Extends**: `RuntimeException`
- **Конструктор**: приймає `OrderStatus currentStatus` та `OrderStatus targetStatus`
- **Message**: формат `"Cannot transition order from CANCELLED to CONFIRMED"`
- **Призначення**: кидається при невалідному переході статусу замовлення

### 1.3 `ErrorResponse`

- **Пакет**: `org.example.orderservice.dto`
- **Поля**: `int status`, `String error`, `String message`, `Instant timestamp`
- **Призначення**: уніфікована структура JSON-відповіді при помилках

### 1.4 `GlobalExceptionHandler`

- **Пакет**: `org.example.orderservice.exception`
- **Анотація**: `@RestControllerAdvice`
- **Обробники**:

| Виняток                            | HTTP статус              |
|------------------------------------|--------------------------|
| `ResourceNotFoundException`        | `404 Not Found`          |
| `OrderStatusException`             | `409 Conflict`           |
| `IllegalArgumentException`         | `400 Bad Request`        |
| `MethodArgumentNotValidException`  | `400 Bad Request`        |
| `Exception` (fallback)             | `500 Internal Server Error` |

- Кожен метод повертає `ResponseEntity<ErrorResponse>`

---

## Фаза 2. Доповнення Domain Entities

Перед створенням сервісів потрібно додати кілька методів в існуючі ентіті.

### 2.1 `OrderEntity` — додати два методи

**Метод `changeStatus(OrderStatus newStatus)`**:
- Валідує переходи між статусами
- Дозволені переходи:
  - `CREATED → CONFIRMED`
  - `CONFIRMED → IN_PROGRESS`
  - `IN_PROGRESS → COMPLETED`
  - `CREATED → CANCELLED`
  - `CONFIRMED → CANCELLED`
- Будь-які інші переходи кидають `OrderStatusException`
- Після валідації присвоює `this.status = newStatus`

**Метод `getTotalPrice()`**:
- Повертає `BigDecimal`
- Рахує суму по всіх `OrderItemEntity`: `item.price * quantity`
- Використовує stream: `items.stream().map(...).reduce(BigDecimal.ZERO, BigDecimal::add)`

### 2.2 `OrderItemEntity` — додати конструктор

- Приймає `ItemEntity item` та `int quantity`
- Присвоює обидва поля

### 2.3 `PaymentEntity` — додати конструктор

- Приймає `BigDecimal amount` та `String method`
- Присвоює обидва поля

---

## Фаза 3. Сервісний шар

Всі сервіси розміщуються в пакеті `org.example.orderservice.service`.
Кожен сервіс анотований `@Service` і використовує constructor injection (`@RequiredArgsConstructor` від Lombok).

### 3.1 `ItemService`

**Залежності**: `ItemRepository`, `ItemMapper`

| Метод                                  | Опис                                              |
|----------------------------------------|---------------------------------------------------|
| `List<ItemResponse> getAllItems()`      | Повертає все меню                                 |
| `ItemResponse getItemById(Long id)`    | За ID, або кидає `ResourceNotFoundException`      |
| `ItemResponse getItemByName(String n)` | Пошук за назвою, або `ResourceNotFoundException`  |

### 3.2 `IngredientService`

**Залежності**: `IngredientRepository`, `IngredientMapper`

| Метод                                              | Опис                                             |
|----------------------------------------------------|--------------------------------------------------|
| `List<IngredientResponse> getAllIngredients()`      | Повертає весь склад                              |
| `IngredientResponse getIngredientById(Long id)`    | За ID, або `ResourceNotFoundException`           |

### 3.3 `OrderService`

**Залежності**: `OrderRepository`, `ItemRepository`, `OrderMapper`

Всі публічні методи позначені `@Transactional`.

| Метод                                                          | Опис                                |
|----------------------------------------------------------------|-------------------------------------|
| `OrderResponse createOrder(CreateOrderRequest request)`        | Створює нове замовлення             |
| `OrderResponse getOrderById(Long id)`                          | За ID (`findByIdWithItems`)         |
| `List<OrderResponse> getOrdersByStatus(OrderStatus status)`    | Фільтрація за статусом             |
| `List<OrderResponse> getAllOrders()`                            | Всі замовлення                     |
| `OrderResponse confirmOrder(Long id)`                          | `CREATED → CONFIRMED`              |
| `OrderResponse cancelOrder(Long id)`                           | `CREATED/CONFIRMED → CANCELLED`    |
| `OrderResponse completeOrder(Long id)`                         | `IN_PROGRESS → COMPLETED`          |

**Деталі `createOrder`:**
1. Створити новий `OrderEntity`
2. Для кожного `OrderItemRequest` з запиту:
   - Знайти `ItemEntity` за `itemId` (або `ResourceNotFoundException`)
   - Створити `OrderItemEntity(item, quantity)`
   - Додати в замовлення через `order.addItem(orderItem)`
3. Зберегти: `orderRepository.save(order)`
4. Конвертувати в `OrderResponse` через `OrderMapper`

**Деталі методів зміни статусу** (`confirmOrder`, `cancelOrder`, `completeOrder`):
1. Знайти замовлення за ID (`findByIdWithItems`)
2. Викликати `order.changeStatus(targetStatus)` — валідація відбувається в ентіті
3. Зберегти: `orderRepository.save(order)`
4. Конвертувати в `OrderResponse`

### 3.4 `PaymentService`

**Залежності**: `PaymentRepository`, `OrderRepository`, `PaymentMapper`

| Метод                                                              | Опис                               |
|--------------------------------------------------------------------|-------------------------------------|
| `PaymentResponse payForOrder(Long orderId, CreatePaymentRequest r)`| Створює оплату для замовлення       |
| `PaymentResponse getPaymentById(Long id)`                          | За ID                              |

**Деталі `payForOrder`:**
1. Знайти `OrderEntity` за `orderId` (або `ResourceNotFoundException`)
2. Перевірити, що статус = `CONFIRMED` (інакше `OrderStatusException`)
3. Перевірити, що `order.getPayment() == null` (інакше `IllegalArgumentException("Order already paid")`)
4. Створити `PaymentEntity(amount, method)`
5. Прив'язати: `order.setPayment(payment)`
6. Змінити статус: `order.changeStatus(IN_PROGRESS)`
7. Зберегти: `orderRepository.save(order)`
8. Конвертувати в `PaymentResponse`

---

## Фаза 4. Валідація вхідних даних

### 4.1 Додати залежність

Додати в `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 4.2 Анотації на DTO

**`CreateOrderRequest`**:
- `items` → `@NotNull @NotEmpty`, елементи → `@Valid`

**`OrderItemRequest`**:
- `itemId` → `@Positive`
- `quantity` → `@Min(1)`

**`CreatePaymentRequest`**:
- `amount` → `@NotNull @Positive`
- `method` → `@NotBlank`

---

## Структура файлів після імплементації

```
src/main/java/org/example/orderservice/
├── config/
│   └── JpaConfig.java
├── domain/
│   ├── base/BaseEntity.java
│   ├── ingredient/IngredientEntity.java
│   ├── item/ItemEntity.java
│   ├── order/OrderEntity.java              ← MODIFIED
│   ├── order/OrderItemEntity.java          ← MODIFIED
│   ├── order/OrderStatus.java
│   └── payment/PaymentEntity.java          ← MODIFIED
├── dto/
│   ├── ErrorResponse.java                  ← NEW
│   ├── ingredient/IngredientResponse.java
│   ├── item/ItemResponse.java
│   ├── order/CreateOrderRequest.java       ← MODIFIED
│   ├── order/OrderItemRequest.java         ← MODIFIED
│   ├── order/OrderItemResponse.java
│   ├── order/OrderResponse.java
│   ├── payment/CreatePaymentRequest.java   ← MODIFIED
│   └── payment/PaymentResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java         ← NEW
│   ├── OrderStatusException.java           ← NEW
│   └── ResourceNotFoundException.java      ← NEW
├── mapper/
│   ├── IngredientMapper.java
│   ├── ItemMapper.java
│   ├── OrderMapper.java
│   └── PaymentMapper.java
├── repository/
│   ├── IngredientRepository.java
│   ├── ItemRepository.java
│   ├── OrderRepository.java
│   └── PaymentRepository.java
├── service/
│   ├── IngredientService.java              ← NEW
│   ├── ItemService.java                    ← NEW
│   ├── OrderService.java                   ← NEW
│   └── PaymentService.java                 ← NEW
└── OrderServiceApplication.java
```

---

## Діаграма залежностей

```
Controller (наступний етап)
    │
    ▼
┌─────────────────────────────────────────┐
│              Service Layer              │
│                                         │
│  ItemService ─────► ItemRepository      │
│       │              ItemMapper         │
│       │                                 │
│  IngredientService ► IngredientRepo     │
│       │              IngredientMapper   │
│       │                                 │
│  OrderService ────► OrderRepository     │
│       │              ItemRepository     │
│       │              OrderMapper        │
│       │                                 │
│  PaymentService ──► PaymentRepository   │
│                      OrderRepository    │
│                      PaymentMapper      │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│          Domain Entities + Repos        │
│              (JPA / PostgreSQL)         │
└─────────────────────────────────────────┘
```

---

## Open Question

> **Кількість інгредієнтів на один item**: Зараз таблиця `item_ingredients` — це M2M
> без колонки `quantity` (скільки грамів зерен потрібно на 1 еспресо).
> Це означає, що на цьому етапі ми НЕ зможемо реалізувати автоматичне списання
> інгредієнтів зі складу при створенні замовлення.
>
> Варіанти:
> 1. Додати міграцію `V3` з колонкою `quantity` в `item_ingredients` зараз
> 2. Відкласти цю логіку на пізніше

---

## Порядок виконання

1. Додати `spring-boot-starter-validation` в `pom.xml`
2. Створити exception класи (`ResourceNotFoundException`, `OrderStatusException`)
3. Створити `ErrorResponse` DTO
4. Створити `GlobalExceptionHandler`
5. Модифікувати ентіті (`OrderEntity`, `OrderItemEntity`, `PaymentEntity`)
6. Додати анотації валідації на DTO
7. Створити `ItemService`
8. Створити `IngredientService`
9. Створити `OrderService`
10. Створити `PaymentService`
11. `mvn clean compile` — перевірити компіляцію
12. Коміт: `git commit -m "Add service layer with exception handling"`
