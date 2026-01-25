BEGIN;

TRUNCATE TABLE vouchers, users RESTART IDENTITY CASCADE;

INSERT INTO users (id, username, first_name, last_name, password, user_role, phone_number, email, balance, user_status, auth_provider)
VALUES
-- admin123
('11111111-1111-1111-1111-111111111111', 'admin_boss', 'Admin', 'Admin', '$2a$12$3e/PrWuIRyLjnGFKRfxfYOo9Kqf8uSSmPAo3vfLdsO6gexNpKDU6.', 'ADMIN', '+1234567890', 'admin@travel.com', 0.00, TRUE, 'LOCAL'),

-- manager123
('22222222-2222-2222-2222-222222222222', 'manager_lisa', 'Lisa', 'Manager', '$2a$12$I2B/SnRFYLi2d5ElTrIfdedCZ8ybSrouvAz/GjKFOmOmcfNTrZIKS', 'MANAGER', '+1987654321', 'lisa@travel.com', 0.00, TRUE, 'LOCAL'),

-- bob123
('33333333-3333-3333-3333-333333333333', 'traveler_bob', 'Bob', 'Traveler', '$2a$12$5ITpyTr6k2GnQPGPueEhTeekbsctqYRwdeRo/eXB6IQKRz25SzumK', 'USER', '+1122334455', 'bob@traveler.com', 5000.00, TRUE, 'LOCAL'),

--  student123
('44444444-4444-4444-4444-444444444444', 'poor_student', 'Denis', 'Radchenko', '$2a$12$ZV6JcqThPUCXALlU.lNBe.lK/RTzjT5EaxrWrIRkSeFA5mMK0sDF.', 'USER', NULL, 'student@uni.edu', 10.00, FALSE, 'LOCAL');

INSERT INTO vouchers (
    title,
    description,
    price,
    voucher_tour_type,
    voucher_transfer_type,
    voucher_hotel_type,
    voucher_status_type,
    arrival_date,
    eviction_date,
    user_id,
    is_hot
)
SELECT
    tour_types[floor(random() * array_length(tour_types, 1) + 1)] || ' Trip #' || i AS title,

    'Enjoy our exclusive ' || lower(tour_types[floor(random() * array_length(tour_types, 1) + 1)]) ||
    ' package. Transfer by ' || lower(transfer_types[floor(random() * array_length(transfer_types, 1) + 1)]) || ' included.',

    (random() * 6900 + 100)::NUMERIC(10, 2) AS price,

    tour_types[floor(random() * array_length(tour_types, 1) + 1)]::tour_type,
    transfer_types[floor(random() * array_length(transfer_types, 1) + 1)]::transfer_type,
    hotel_types[floor(random() * array_length(hotel_types, 1) + 1)]::hotel_type,
    status_types[floor(random() * array_length(status_types, 1) + 1)]::status_type,

    gen.arrival AS arrival_date,
    gen.arrival + (floor(random() * 14) + 1 || ' days')::INTERVAL AS eviction_date,

    user_ids[floor(random() * array_length(user_ids, 1) + 1)] AS user_id,
    (random() < 0.25) AS is_hot

FROM generate_series(1, 120) AS i
         CROSS JOIN (
    SELECT
        ARRAY['HEALTH', 'SPORTS', 'LEISURE', 'SAFARI', 'WINE', 'ECO', 'ADVENTURE', 'CULTURAL'] AS tour_types,
        ARRAY['BUS', 'TRAIN', 'PLANE', 'SHIP', 'PRIVATE_CAR', 'JEEPS', 'MINIBUS', 'ELECTRICAL_CARS'] AS transfer_types,
        ARRAY['ONE_STARS', 'TWO_STARS', 'THREE_STARS', 'FOUR_STARS', 'FIVE_STARS'] AS hotel_types,
        ARRAY['CREATED', 'REGISTERED', 'PAID', 'CANCELED'] AS status_types,
        ARRAY[
            '11111111-1111-1111-1111-111111111111'::uuid,
            '22222222-2222-2222-2222-222222222222'::uuid,
            '33333333-3333-3333-3333-333333333333'::uuid,
            '44444444-4444-4444-4444-444444444444'::uuid,
            NULL
            ] AS user_ids
) AS data_source
         CROSS JOIN LATERAL (
    SELECT ('2026-01-01'::date + (random() * 350)::int + (i * 0)) AS arrival
    ) AS gen;

COMMIT;