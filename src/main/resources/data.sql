-- Очистка таблиц (если нужно)
DELETE FROM reservation_user;
DELETE FROM reservations;
DELETE FROM users;
DELETE FROM coworking_spaces;

-- Сброс последовательностей (для PostgreSQL)
ALTER SEQUENCE coworking_spaces_id_seq RESTART WITH 1;
ALTER SEQUENCE reservations_id_seq RESTART WITH 1;
ALTER SEQUENCE users_id_seq RESTART WITH 1;

-- Коворкинг-пространства
INSERT INTO coworking_spaces (name, address)
VALUES
    ('Space X', 'ул. Пушкина, 10'),
    ('Creative Hub', 'пр. Ленина, 25');

-- Пользователи
INSERT INTO users (full_name, email, password)
VALUES
    ('Иван Иванов', 'ivan@mail.com', 'password123'),
    ('Мария Петрова', 'maria@mail.com', 'qwerty');

-- Резервации
INSERT INTO reservations (reservation_date, coworking_space_id)
VALUES
    ('2023-12-25', 1),
    ('2023-12-26', 2);

-- Связи резерваций и пользователей (reservation_user)
INSERT INTO reservation_user (reservation_id, user_id)
VALUES
    (1, 1),
    (1, 2),
    (2, 1);