-- Додаємо інгредієнти
INSERT INTO ingredients (type, name, stock_count) VALUES
('BEANS', 'Кавові зерна', 1000),
('LIQUID', 'Вода', 5000),
('LIQUID', 'Молоко', 2000),
('ADDITIVE', 'Цукор', 500);

-- Додаємо напої (Items)
INSERT INTO items (type, name, price) VALUES
('DRINK', 'Еспресо', 45.00),
('DRINK', 'Американо', 50.00),
('DRINK', 'Капучіно', 65.00),
('DRINK', 'Лате', 70.00);

-- Зв'язуємо напої з інгредієнтами (Еспресо: Зерна, Вода)
INSERT INTO item_ingredients (item_id, ingredient_id)
SELECT i.id, ing.id FROM items i, ingredients ing 
WHERE i.name = 'Еспресо' AND ing.name IN ('Кавові зерна', 'Вода');

-- Американо: Зерна, Вода
INSERT INTO item_ingredients (item_id, ingredient_id)
SELECT i.id, ing.id FROM items i, ingredients ing 
WHERE i.name = 'Американо' AND ing.name IN ('Кавові зерна', 'Вода');

-- Капучіно: Зерна, Вода, Молоко
INSERT INTO item_ingredients (item_id, ingredient_id)
SELECT i.id, ing.id FROM items i, ingredients ing 
WHERE i.name = 'Капучіно' AND ing.name IN ('Кавові зерна', 'Вода', 'Молоко');

-- Лате: Зерна, Вода, Молоко
INSERT INTO item_ingredients (item_id, ingredient_id)
SELECT i.id, ing.id FROM items i, ingredients ing 
WHERE i.name = 'Лате' AND ing.name IN ('Кавові зерна', 'Вода', 'Молоко');
