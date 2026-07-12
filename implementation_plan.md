# DTO та Mapping Layer — План Імплементації

## Контекст

Проєкт має 5 доменних сутностей: `IngredientEntity`, `ItemEntity`, `OrderEntity`, `OrderItemEntity`, `PaymentEntity`. Усі наслідують `BaseEntity` (id, createdAt, lastModifiedAt). Наступний крок — створити DTO-шар для безпечної передачі даних через REST API та маппери для конвертації Entity ↔ DTO.

---

## Рішення які потрібно прийняти перед початком

> [!IMPORTANT]
> **1. MapStruct vs Ручний маппінг**
> - **MapStruct** — compile-time генератор. Плюси: мінімум boilerplate, підтримка вкладених об'єктів. Мінуси: додаткова залежність, потрібна конфігурація annotation processor з Lombok.
> - **Ручний маппінг** — статичні методи або Spring-компоненти. Плюси: повний контроль, 0 залежностей. Мінуси: більше коду, ризик помилок при зміні полів.

> [!IMPORTANT]
> **2. Java Records vs Lombok-класи для DTO**
> - **Records** (Java 17) — дають immutability, `equals()`, `hashCode()`, `toString()` з коробки. Ідеально для DTO.
> - **Lombok `@Value`** — аналогічна функціональність, але через вже використовувану бібліотеку.

---

## Крок 1: Додати залежність MapStruct (якщо обрано MapStruct)

**Файл:** [pom.xml](file:///home/anonim/IdeaProjects/order_service/pom.xml)

**Що зробити:**
1. Додати property `mapstruct.version` зі значенням `1.5.5.Final` у блок `<properties>`
2. Додати залежність `org.mapstruct:mapstruct` у блок `<dependencies>`
3. Додати плагін `maven-compiler-plugin` з `<annotationProcessorPaths>`, який містить три процесори **в такому порядку**:
   - `org.projectlombok:lombok` (версія `${lombok.version}`)
   - `org.mapstruct:mapstruct-processor` (версія `${mapstruct.version}`)
   - `org.projectlombok:lombok-mapstruct-binding` (версія `0.2.0`)

> [!WARNING]
> Порядок annotation processors критичний: Lombok має йти **перед** MapStruct, інакше MapStruct не побачить згенеровані Lombok-ом геттери.

---

## Крок 2: Створити пакетну структуру

**Що зробити:** Створити нові пакети:
- `org.example.orderservice.dto.ingredient`
- `org.example.orderservice.dto.item`
- `org.example.orderservice.dto.order`
- `org.example.orderservice.dto.payment`
- `org.example.orderservice.mapper`

---

## Крок 3: DTO для Ingredient

**Новий файл:** `dto/ingredient/IngredientResponse.java`

**Поля:** `id` (Long), `type` (String), `name` (String), `stockCount` (int)

> [!NOTE]
> Request DTO для інгредієнтів **не потрібен** на цьому етапі — вони наповнюються через seed-міграції. Додамо пізніше, якщо буде адмін-панель.

---

## Крок 4: DTO для Item

**Новий файл:** `dto/item/ItemResponse.java`

**Поля:** `id` (Long), `type` (String), `name` (String), `price` (BigDecimal), `ingredients` (List\<IngredientResponse\>)

> [!NOTE]
> `ingredients` — вкладений список. MapStruct автоматично замапить `Set<IngredientEntity>` → `List<IngredientResponse>`, якщо `IngredientMapper` вже існує і підключений через `uses`.

---

## Крок 5: DTO для Order

Замовлення — головний агрегат, потрібні і Request, і Response DTO.

### Request DTO

| Файл | Поля |
|------|------|
| `dto/order/CreateOrderRequest.java` | `items` (List\<OrderItemRequest\>) |
| `dto/order/OrderItemRequest.java` | `itemId` (Long), `quantity` (int) |

### Response DTO

| Файл | Поля |
|------|------|
| `dto/order/OrderResponse.java` | `id` (Long), `status` (String), `items` (List\<OrderItemResponse\>), `payment` (PaymentResponse — nullable), `createdAt` (Instant) |
| `dto/order/OrderItemResponse.java` | `id` (Long), `itemName` (String), `itemPrice` (BigDecimal), `quantity` (int) |

> [!TIP]
> В `OrderItemResponse` поля `itemName` та `itemPrice` денормалізовані з вкладеного `ItemEntity`, щоб клієнт не робив додатковий запит. При маппінгу потрібно вказати `@Mapping(source = "item.name", target = "itemName")`.

---

## Крок 6: DTO для Payment

| Файл | Поля |
|------|------|
| `dto/payment/CreatePaymentRequest.java` | `amount` (BigDecimal), `method` (String) |
| `dto/payment/PaymentResponse.java` | `id` (Long), `amount` (BigDecimal), `method` (String), `createdAt` (Instant) |

---

## Крок 7: MapStruct Маппери

Усі маппери створюються у пакеті `org.example.orderservice.mapper` як інтерфейси з анотацією `@Mapper(componentModel = "spring")`.

### IngredientMapper

| Метод | Опис |
|-------|------|
| `toResponse(IngredientEntity)` → `IngredientResponse` | Пряме відображення 1:1 — MapStruct замапить автоматично |

### ItemMapper

| Метод | Опис |
|-------|------|
| `toResponse(ItemEntity)` → `ItemResponse` | Потрібно вказати `uses = IngredientMapper.class`, щоб MapStruct замапив вкладений `Set<IngredientEntity>` → `List<IngredientResponse>` |

### PaymentMapper

| Метод | Опис |
|-------|------|
| `toResponse(PaymentEntity)` → `PaymentResponse` | Пряме відображення 1:1 |

### OrderMapper (найскладніший)

| Метод | Опис |
|-------|------|
| `toResponse(OrderEntity)` → `OrderResponse` | Потрібно вказати `uses = {PaymentMapper.class}` та додати внутрішній маппінг для `OrderItemEntity` → `OrderItemResponse` |
| `toOrderItemResponse(OrderItemEntity)` → `OrderItemResponse` | Маппінг з `@Mapping`: `item.name` → `itemName`, `item.price` → `itemPrice` |

> [!CAUTION]
> **НЕ** створюй маппінг `CreateOrderRequest` → `OrderEntity`. Створення замовлення потребує бізнес-логіки (перевірка наявності товарів, підрахунок суми, дедукція стоку) — це буде відповідальність сервісного шару.

---

## Підсумок: повний список файлів

| # | Файл | Дія |
|---|------|-----|
| 1 | `pom.xml` | MODIFY — додати MapStruct |
| 2 | `dto/ingredient/IngredientResponse.java` | NEW |
| 3 | `dto/item/ItemResponse.java` | NEW |
| 4 | `dto/order/CreateOrderRequest.java` | NEW |
| 5 | `dto/order/OrderItemRequest.java` | NEW |
| 6 | `dto/order/OrderResponse.java` | NEW |
| 7 | `dto/order/OrderItemResponse.java` | NEW |
| 8 | `dto/payment/CreatePaymentRequest.java` | NEW |
| 9 | `dto/payment/PaymentResponse.java` | NEW |
| 10 | `mapper/IngredientMapper.java` | NEW |
| 11 | `mapper/ItemMapper.java` | NEW |
| 12 | `mapper/OrderMapper.java` | NEW |
| 13 | `mapper/PaymentMapper.java` | NEW |

---

## Верифікація

1. **`mvn compile`** — переконатися, що MapStruct процесор згенерував реалізації без помилок
2. Перевірити директорію `target/generated-sources/annotations/` — там мають з'явитися `*MapperImpl.java` класи
3. Переглянути згенерований код `OrderMapperImpl.java` — переконатися, що `itemName` та `itemPrice` маппляться коректно з вкладеного `ItemEntity`
