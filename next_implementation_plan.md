# Next Implementation Plan

Цей документ описує, що робити після завершення CLI-версії `order_service`.
Мета наступного етапу: зробити ядро системи чистішим, протестованим і готовим до майбутнього REST API або бази даних.

Поточний стан:

- CLI вже запускається через `Main`.
- Дані ініціалізуються в `src/main/java/org/example/cli/DataInitializer.java`.
- Меню, склад, замовлення і оплата вже взаємодіють між собою.
- Замовлення зараз зберігає `List<Item>`, тобто якщо користувач замовляє 3 еспресо, один і той самий `Item` додається у список 3 рази.
- `InventoryService.makeOrder(order)` повертає `false`, якщо інгредієнтів недостатньо, але не пояснює, яких саме інгредієнтів бракує.

---

## Загальні правила роботи

Перед кожною великою зміною:

1. Перевірити статус:

```bash
git status
```

2. Переконатися, що немає незрозумілих змін.

3. Після завершення кожної фази запускати:

```bash
mvn test
```

Якщо Maven має проблему з локальним `~/.m2`, використовувати:

```bash
mvn -Dmaven.repo.local=/tmp/order_service_m2 test
```

4. Після завершення логічної фази робити окремий коміт:

```bash
git add .
git commit -m "Meaningful commit message"
```

---

## Фаза 1. Додати unit-тести для поточного ядра

### Навіщо це потрібно

Перед тим як міняти модель замовлення, треба зафіксувати поточну поведінку тестами.
Так буде легше зрозуміти, чи ми нічого не зламали під час рефакторингу.

### Що створити

Створити папки:

```text
src/test/java/org/example/inventoryService/
src/test/java/org/example/orderService/
```

Створити файли:

```text
src/test/java/org/example/inventoryService/InventoryServiceTest.java
src/test/java/org/example/orderService/OrderTest.java
```

### Тест 1. `OrderTest`

Файл:

```text
src/test/java/org/example/orderService/OrderTest.java
```

Що перевірити:

- порожнє замовлення має суму `BigDecimal.ZERO`;
- після додавання одного `Item` сума дорівнює ціні item-а;
- після додавання кількох `Item` сума дорівнює сумі їхніх цін;
- `removeItem(name)` видаляє позицію з замовлення.

Орієнтовна структура:

```java
package org.example.orderService;

import org.inventoryService.Ingredient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {
    @Test
    void getTotalPriceReturnsZeroForEmptyOrder() {
        Order order = new Order(1);

        assertEquals(BigDecimal.ZERO, order.getTotalPrice());
    }
}
```

Допоміжний метод у тесті:

```java
private Item item(String name, String price) {
    return new Item(
            "Напій",
            name,
            1,
            List.of(new Ingredient("Зерна", "Кавові зерна", 10)),
            new BigDecimal(price)
    );
}
```

### Тест 2. `InventoryServiceTest`

Файл:

```text
src/test/java/org/example/inventoryService/InventoryServiceTest.java
```

Що перевірити:

- `isValidOrder(order)` повертає `true`, якщо інгредієнтів достатньо;
- `isValidOrder(order)` повертає `false`, якщо інгредієнтів недостатньо;
- `makeOrder(order)` списує інгредієнти зі складу;
- `makeOrder(order)` не списує інгредієнти, якщо замовлення невалідне;
- якщо замовлення містить кілька item-ів, сервіс правильно сумує потребу в інгредієнтах.

Орієнтовна структура:

```java
package org.example.inventoryService;

import org.orderService.Item;
import org.orderService.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryServiceTest {
    @Test
    void makeOrderUsesIngredientsWhenInventoryHasEnoughStock() {
        Inventory inventory = new Inventory();
        inventory.addItem(new Ingredient("Зерна", "Кавові зерна", 100));

        InventoryService service = new InventoryService(inventory);
        Order order = new Order(1);
        order.addItem(itemWithCoffee(20));

        boolean result = service.makeOrder(order);

        assertTrue(result);
        assertEquals(80, inventory.getIngredient("Кавові зерна").orElseThrow().getCount());
    }
}
```

Допоміжний метод:

```java
private Item itemWithCoffee(int coffeeCount) {
    return new Item(
            "Напій",
            "Тестова кава",
            1,
            List.of(new Ingredient("Зерна", "Кавові зерна", coffeeCount)),
            new BigDecimal("50.00")
    );
}
```

### Як перевірити фазу

Запустити:

```bash
mvn test
```

Очікуваний результат:

- build success;
- всі тести зелені.

### Коміт після фази

```bash
git add src/test/java
git commit -m "Add domain service tests"
```

---

## Фаза 2. Ввести модель `OrderItem`

### Навіщо це потрібно

Зараз кількість товарів у замовленні моделюється повторним додаванням `Item`.
Наприклад, 3 капучіно зберігаються як:

```text
[Капучіно, Капучіно, Капучіно]
```

Це працює, але незручно для:

- чеків;
- REST API;
- історії замовлень;
- бази даних;
- правильного оновлення кількості.

Після зміни має бути:

```text
Order
└── List<OrderItem>
    ├── Item: Капучіно
    └── quantity: 3
```

### Що створити

Створити файл:

```text
src/main/java/org/example/orderService/OrderItem.java
```

### Що написати в `OrderItem`

Клас має містити:

- поле `Item item`;
- поле `int quantity`;
- конструктор;
- getters;
- метод `getTotalPrice()`;
- метод `increaseQuantity(int amount)`;
- валідацію, що quantity більше нуля.

Орієнтовна реалізація:

```java
package org.example.orderService;

import java.math.BigDecimal;

public class OrderItem {
    private final Item item;
    private int quantity;

    public OrderItem(Item item, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        this.quantity += amount;
    }

    public BigDecimal getTotalPrice() {
        return item.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
```

### Що змінити в `Order`

Файл:

```text
src/main/java/org/example/orderService/Order.java
```

Зараз:

```java
private final List<Item> order = new ArrayList<>();
```

Потрібно замінити на:

```java
private final List<OrderItem> items = new ArrayList<>();
```

Методи після зміни:

```java
public List<OrderItem> getItems()
```

Повертає unmodifiable list.

```java
public BigDecimal getTotalPrice()
```

Сумує `orderItem.getTotalPrice()`.

```java
public void addItem(Item item, int quantity)
```

Якщо item з такою назвою вже є в замовленні, збільшує quantity.
Якщо нема, додає новий `OrderItem`.

```java
public void removeItem(String name)
```

Видаляє `OrderItem`, у якого `orderItem.getItem().getName().equals(name)`.

### Важливо

На цьому етапі можна тимчасово залишити старий метод:

```java
public void addItem(Item item)
```

Але він має просто викликати:

```java
addItem(item, 1);
```

Це допоможе не ламати весь код одразу.

### Що змінити в `InventoryService`

Файл:

```text
src/main/java/org/example/inventoryService/InventoryService.java
```

Зараз сервіс проходиться по:

```java
for (Item item : order.getOrder())
```

Після зміни потрібно проходитися по:

```java
for (OrderItem orderItem : order.getItems())
```

І використовувати:

```java
Item item = orderItem.getItem();
int quantity = orderItem.getQuantity();
```

Розрахунок потреби:

```java
int currentRequired = ingredient.getCount() * quantity;
```

### Що змінити в `ConsoleUI`

Файл:

```text
src/main/java/org/example/cli/ConsoleUI.java
```

Методи, які треба адаптувати:

- `printOrderDraft(Order order)`;
- `printReceipt(Order order, Payment payment)`;
- приватний метод `printOrderItems(Order order)`.

Замість групування повторюваних `Item` через `Map<String, OrderLine>` треба просто пройтися по:

```java
for (OrderItem orderItem : order.getItems())
```

І друкувати:

```java
orderItem.getItem().getName()
orderItem.getQuantity()
orderItem.getItem().getPrice()
orderItem.getTotalPrice()
```

Після цього приватний клас `OrderLine` більше не потрібен.

### Що змінити в `Application`

Файл:

```text
src/main/java/org/example/cli/Application.java
```

Зараз:

```java
for (int i = 0; i < quantity; i++) {
    order.addItem(selectedItem.get());
}
```

Потрібно замінити на:

```java
order.addItem(selectedItem.get(), quantity);
```

Також перевірити місця, де використовується:

```java
order.getOrder()
```

Після рефакторингу має бути:

```java
order.getItems()
```

### Що змінити в тестах

Оновити `OrderTest`:

- перевірити, що `addItem(item, 3)` створює один `OrderItem`;
- перевірити, що повторне `addItem(item, 2)` збільшує quantity;
- перевірити, що `getTotalPrice()` множить ціну на quantity.

Оновити `InventoryServiceTest`:

- перевірити, що при quantity 3 інгредієнти списуються 3 рази.

### Як перевірити фазу

```bash
mvn test
```

Потім вручну запустити CLI:

```bash
mvn clean package
java -jar target/order_service-1.0-SNAPSHOT.jar
```

Сценарій ручної перевірки:

1. Обрати `2. Створити замовлення`.
2. Обрати `Капучіно`.
3. Ввести кількість `3`.
4. Завершити замовлення.
5. Оплатити.
6. У чеку має бути один рядок `Капучіно`, кількість `3`, сума `165.00`.

### Коміт після фази

```bash
git add src/main/java src/test/java
git commit -m "Add order item quantity model"
```

---

## Фаза 3. Додати детальну інформацію про нестачу інгредієнтів

### Навіщо це потрібно

Зараз користувач бачить тільки:

```text
Помилка: недостатньо інгредієнтів на складі.
```

Краще показувати конкретно:

```text
Недостатньо інгредієнтів:
- Молоко: потрібно 300, доступно 100
- Кавові зерна: потрібно 45, доступно 20
```

### Що створити

Створити файл:

```text
src/main/java/org/example/inventoryService/MissingIngredient.java
```

### Що написати в `MissingIngredient`

Поля:

- `String name`;
- `int requiredCount`;
- `int availableCount`;

Методи:

- constructor;
- getters;
- `getMissingCount()`.

Орієнтовна реалізація:

```java
package org.example.inventoryService;

public class MissingIngredient {
    private final String name;
    private final int requiredCount;
    private final int availableCount;

    public MissingIngredient(String name, int requiredCount, int availableCount) {
        this.name = name;
        this.requiredCount = requiredCount;
        this.availableCount = availableCount;
    }

    public String getName() {
        return name;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public int getMissingCount() {
        return requiredCount - availableCount;
    }
}
```

### Що змінити в `InventoryService`

Файл:

```text
src/main/java/org/example/inventoryService/InventoryService.java
```

Додати метод:

```java
public List<MissingIngredient> getMissingIngredients(Order order)
```

Логіка:

1. Порахувати загальну потребу по кожному інгредієнту.
2. Для кожного інгредієнта перевірити доступну кількість на складі.
3. Якщо потрібно більше, ніж доступно, додати `MissingIngredient`.
4. Повернути список.

Після цього `isValidOrder(order)` можна переписати простіше:

```java
public boolean isValidOrder(Order order) {
    return getMissingIngredients(order).isEmpty();
}
```

`makeOrder(order)` лишається:

```java
if (!isValidOrder(order)) {
    return false;
}
```

### Що змінити в `ConsoleUI`

Файл:

```text
src/main/java/org/example/cli/ConsoleUI.java
```

Додати метод:

```java
public void printMissingIngredients(List<MissingIngredient> missingIngredients)
```

Вивід:

```text
Недостатньо інгредієнтів:
Назва            Потрібно   Доступно   Бракує
Молоко                300        100      200
```

### Що змінити в `Application`

Файл:

```text
src/main/java/org/example/cli/Application.java
```

У методі `completeOrder(order)` перед `makeOrder(order)` додати перевірку:

```java
List<MissingIngredient> missingIngredients = inventoryService.getMissingIngredients(order);
if (!missingIngredients.isEmpty()) {
    consoleUI.printMissingIngredients(missingIngredients);
    inputHandler.waitForEnter();
    return;
}
```

Після цього можна викликати:

```java
inventoryService.makeOrder(order);
```

### Що додати в тести

У `InventoryServiceTest` додати:

- тест, що `getMissingIngredients(order)` повертає порожній список, якщо всього достатньо;
- тест, що метод повертає правильну назву інгредієнта;
- тест, що метод повертає `requiredCount`;
- тест, що метод повертає `availableCount`;
- тест, що метод повертає `missingCount`.

### Як перевірити фазу

Автоматично:

```bash
mvn test
```

Ручний сценарій:

1. У `DataInitializer` тимчасово зменшити кількість молока до `100`.
2. Запустити програму.
3. Спробувати замовити 2 капучіно.
4. CLI має показати, що бракує молока.
5. Повернути кількість молока назад.

### Коміт після фази

```bash
git add src/main/java src/test/java
git commit -m "Report missing ingredients"
```

---

## Фаза 4. Винести оплату в `PaymentService`

### Навіщо це потрібно

Зараз `Application` напряму створює:

```java
new Payment(...)
```

CLI не повинен містити бізнес-логіку створення оплати.
Краще винести це в окремий сервіс.

### Що створити

Створити файл:

```text
src/main/java/org/example/paymentService/PaymentService.java
```

### Що написати в `PaymentService`

Поля:

- `int nextPaymentId = 1`.

Метод:

```java
public Payment pay(Order order, String method)
```

Логіка:

1. Створити payment id.
2. Взяти суму з `order.getTotalPrice()`.
3. Створити `Payment`.
4. Повернути `Payment`.

Орієнтовна реалізація:

```java
package org.example.paymentService;

import org.orderService.Order;

public class PaymentService {
    private int nextPaymentId = 1;

    public Payment pay(Order order, String method) {
        String paymentId = "PAY-" + nextPaymentId++;
        return new Payment(paymentId, order.getTotalPrice(), method);
    }
}
```

### Що змінити в `Application`

Файл:

```text
src/main/java/org/example/cli/Application.java
```

Додати поле:

```java
private final PaymentService paymentService;
```

Оновити конструктор:

```java
public Application(Menu menu, Inventory inventory, InventoryService inventoryService, PaymentService paymentService)
```

У `completeOrder(order)` замінити ручне створення `Payment` на:

```java
Payment payment = paymentService.pay(order, "CASH");
```

### Що змінити в `Main`

Файл:

```text
src/main/java/org/example/Main.java
```

Створити:

```java
PaymentService paymentService = new PaymentService();
```

Передати його в `Application`.

### Альтернативний варіант

Можна додати `PaymentService` у `DataInitializer`, як уже зроблено з `InventoryService`.
Тоді `Main` буде брати:

```java
dataInitializer.getPaymentService()
```

Цей варіант кращий, якщо ми хочемо, щоб `DataInitializer` збирав усі залежності застосунку.

### Що додати в тести

Створити:

```text
src/test/java/org/example/paymentService/PaymentServiceTest.java
```

Перевірити:

- `pay(order, "CASH")` повертає payment з правильною сумою;
- payment id не порожній;
- method дорівнює `"CASH"`.

### Як перевірити фазу

```bash
mvn test
java -jar target/order_service-1.0-SNAPSHOT.jar
```

Ручний сценарій:

1. Створити замовлення.
2. Оплатити.
3. Переконатися, що чек друкується як раніше.

### Коміт після фази

```bash
git add src/main/java src/test/java
git commit -m "Add payment service"
```

---

## Фаза 5. Додати історію замовлень

### Навіщо це потрібно

Поки програма працює in-memory, але після оплати замовлення ніде не зберігається.
Для CLI і майбутнього REST API корисно мати історію:

- номер замовлення;
- список позицій;
- сума;
- payment id;
- метод оплати.

### Що створити

Створити файл:

```text
src/main/java/org/example/orderService/OrderHistory.java
```

### Що написати в `OrderHistory`

Поля:

```java
private final List<Order> paidOrders = new ArrayList<>();
```

Методи:

```java
public void add(Order order)
public List<Order> getPaidOrders()
public Optional<Order> findById(int orderId)
```

Повернення списку:

```java
return Collections.unmodifiableList(paidOrders);
```

### Що змінити в `Application`

Файл:

```text
src/main/java/org/example/cli/Application.java
```

Додати поле:

```java
private final OrderHistory orderHistory;
```

Після успішної оплати:

```java
orderHistory.add(order);
```

Додати нову команду в `handleCommand`:

```java
case 4 -> showOrderHistory();
```

Оновити головне меню:

```text
4. Історія замовлень
```

Додати метод:

```java
private void showOrderHistory()
```

### Що змінити в `ConsoleUI`

Файл:

```text
src/main/java/org/example/cli/ConsoleUI.java
```

Додати метод:

```java
public void printOrderHistory(List<Order> orders)
```

Вивід:

```text
Історія замовлень:
#      Позицій      Сума
1           2      90.00
2           1      55.00
```

Позицій можна рахувати як суму quantity по всіх `OrderItem`.

### Що змінити в `DataInitializer`

Файл:

```text
src/main/java/org/example/cli/DataInitializer.java
```

Якщо хочемо збирати всі залежності тут, додати:

```java
private final OrderHistory orderHistory;
```

У конструктор:

```java
this.orderHistory = new OrderHistory();
```

Getter:

```java
public OrderHistory getOrderHistory()
```

### Що додати в тести

Створити:

```text
src/test/java/org/example/orderService/OrderHistoryTest.java
```

Перевірити:

- нова історія порожня;
- `add(order)` додає замовлення;
- `findById(orderId)` знаходить замовлення;
- `findById(unknownId)` повертає empty optional.

### Як перевірити фазу

```bash
mvn test
mvn clean package
java -jar target/order_service-1.0-SNAPSHOT.jar
```

Ручний сценарій:

1. Створити і оплатити перше замовлення.
2. Створити і оплатити друге замовлення.
3. Обрати `4. Історія замовлень`.
4. Побачити обидва замовлення.

### Коміт після фази

```bash
git add src/main/java src/test/java
git commit -m "Add in-memory order history"
```

---

## Фаза 6. Покращити CLI після змін ядра

### Навіщо це потрібно

Після `OrderItem`, `PaymentService` і `OrderHistory` CLI має залишитися простим і зрозумілим.
Ця фаза про зручність, а не про нову бізнес-логіку.

### Що змінити в `InputHandler`

Файл:

```text
src/main/java/org/example/cli/InputHandler.java
```

Додати метод:

```java
public boolean readConfirmation(String prompt)
```

Логіка:

- приймати `y`, `yes`, `так`, `т`;
- приймати `n`, `no`, `ні`, `н`;
- якщо введення невідоме, просити повторити.

Тоді в `Application` замість:

```java
String confirmation = inputHandler.readString("Оплатити замовлення? (y/n): ");
if (!confirmation.equalsIgnoreCase("y")) {
```

буде:

```java
if (!inputHandler.readConfirmation("Оплатити замовлення? (y/n): ")) {
```

### Що змінити в `ConsoleUI`

Файл:

```text
src/main/java/org/example/cli/ConsoleUI.java
```

Перевірити форматування:

- меню;
- склад;
- чек;
- історія замовлень;
- нестача інгредієнтів.

Всі таблиці мають мати:

- заголовок;
- рівні колонки;
- зрозумілі назви.

### Чого не робити без потреби

Не додавати агресивне очищення консолі, якщо воно погано працює в IDE.
Можна додати окремий метод:

```java
public void printSeparator()
```

І друкувати:

```text
----------------------------------------------
```

Це надійніше, ніж ANSI clear screen.

### Як перевірити фазу

Ручний сценарій:

1. Ввести не число в головному меню.
2. Ввести неіснуючий номер позиції.
3. Ввести quantity `0`.
4. Скасувати замовлення.
5. Підтвердити замовлення через `y`.
6. Відмовитись від замовлення через `n`.

CLI не має падати.

### Коміт після фази

```bash
git add src/main/java
git commit -m "Improve CLI input flow"
```

---

## Фаза 7. Підготувати код до REST API

### Навіщо це потрібно

Після CLI можна переходити до Spring Boot або іншого HTTP-шару.
Щоб перехід був легким, бізнес-логіка не має залежати від консолі.

### Що перевірити

CLI-пакет:

```text
src/main/java/org/example/cli/
```

Може залежати від:

```text
inventoryService
orderService
paymentService
```

Але доменні пакети не мають залежати від CLI.

Правильно:

```text
Application -> InventoryService
Application -> Order
Application -> PaymentService
```

Неправильно:

```text
InventoryService -> ConsoleUI
Order -> InputHandler
PaymentService -> Application
```

### Можлива структура для майбутнього REST API

Пізніше можна буде додати:

```text
src/main/java/org/example/api/
├── MenuController.java
├── InventoryController.java
├── OrderController.java
└── PaymentController.java
```

Але це окрема велика фаза.
Не треба додавати Spring Boot, поки ядро не покрите тестами.

---

## Фаза 8. Майбутній REST API

Ця фаза не є обов'язковою прямо зараз, але це логічний наступний великий крок.

### Потенційні endpoints

```text
GET  /menu
GET  /inventory
POST /orders
GET  /orders
GET  /orders/{id}
POST /orders/{id}/pay
```

### Приклад request для створення замовлення

```json
{
  "items": [
    {
      "name": "Капучіно",
      "quantity": 2
    },
    {
      "name": "Еспресо",
      "quantity": 1
    }
  ]
}
```

### Приклад response

```json
{
  "orderId": 1,
  "items": [
    {
      "name": "Капучіно",
      "quantity": 2,
      "price": 55.00,
      "total": 110.00
    },
    {
      "name": "Еспресо",
      "quantity": 1,
      "price": 35.00,
      "total": 35.00
    }
  ],
  "totalPrice": 145.00
}
```

### Що треба мати перед REST API

Перед переходом до REST бажано завершити:

- Фазу 1: unit-тести;
- Фазу 2: `OrderItem`;
- Фазу 3: детальні помилки складу;
- Фазу 4: `PaymentService`;
- Фазу 5: історію замовлень.

---

## Рекомендований порядок виконання

Найкращий порядок:

1. Додати тести для поточного ядра.
2. Ввести `OrderItem`.
3. Оновити `InventoryService` під `OrderItem`.
4. Оновити CLI під `OrderItem`.
5. Додати детальні помилки складу.
6. Винести оплату в `PaymentService`.
7. Додати історію замовлень.
8. Покращити CLI input flow.
9. Лише після цього думати про REST API.

---

## Definition of Done для наступного етапу

Етап можна вважати завершеним, коли:

- є unit-тести для `Order`;
- є unit-тести для `InventoryService`;
- `Order` використовує `OrderItem`;
- кількість позицій у замовленні не моделюється повторним додаванням одного `Item`;
- CLI показує правильну кількість і суму в чеку;
- при нестачі інгредієнтів видно, чого саме бракує;
- оплату створює `PaymentService`;
- успішні замовлення зберігаються в in-memory історії;
- `mvn test` проходить;
- `mvn clean package` проходить;
- `java -jar target/order_service-1.0-SNAPSHOT.jar` запускає програму.
