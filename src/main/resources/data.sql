BEGIN;

TRUNCATE TABLE vouchers, users RESTART IDENTITY CASCADE;

INSERT INTO users (id, username, password, user_role, phone_number, email, balance, user_status)
VALUES
-- admin123
('11111111-1111-1111-1111-111111111111', 'admin_boss', '$2a$12$3e/PrWuIRyLjnGFKRfxfYOo9Kqf8uSSmPAo3vfLdsO6gexNpKDU6.', 'ADMIN', '+1234567890', 'admin@travel.com', 0.00, TRUE),

-- manager123
('22222222-2222-2222-2222-222222222222', 'manager_lisa', '$2a$12$I2B/SnRFYLi2d5ElTrIfdedCZ8ybSrouvAz/GjKFOmOmcfNTrZIKS', 'MANAGER', '+1987654321', 'lisa@travel.com', 0.00, TRUE),

-- bob123
('33333333-3333-3333-3333-333333333333', 'traveler_bob', '$2a$12$5ITpyTr6k2GnQPGPueEhTeekbsctqYRwdeRo/eXB6IQKRz25SzumK', 'USER', '+1122334455', 'bob@gmail.com', 5000.00, TRUE),

--  student123
('44444444-4444-4444-4444-444444444444', 'poor_student', '$2a$12$ZV6JcqThPUCXALlU.lNBe.lK/RTzjT5EaxrWrIRkSeFA5mMK0sDF.', 'USER', NULL, 'student@uni.edu', 10.00, FALSE);


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
VALUES

(
    'Week in Paris',
    'Romantic trip specifically for wine lovers and art enthusiasts.',
    1200.50,
    'WINE',
    'PLANE',
    'FOUR_STAR',
    'PAID',
    '2024-05-01',
    '2024-05-08',
    '33333333-3333-3333-3333-333333333333',
    FALSE
),

(
    'Safari in Kenya',
    'Unforgettable adventure with wild animals.',
    2500.00,
    'SAFARI',
    'JEEPS',
    'THREE_STAR',
    'REGISTERED',
    '2024-08-10',
    '2024-08-20',
    '33333333-3333-3333-3333-333333333333',
    TRUE
),

(
    'Relax in Maldives',
    'Just ocean, sun and cocktails.',
    3000.00,
    'HEALTH',
    'PLANE',
    'FIVE_STAR',
    'REGISTERED',
    '2024-12-01',
    '2024-12-10',
    NULL,
    TRUE
),

(
    'Bus tour to Krakow',
    'Cheap weekend trip.',
    150.00,
    'CULTURAL',
    'BUS',
    'ONE_STAR',
    'CANCELED',
    '2024-03-01',
    '2024-03-03',
    '44444444-4444-4444-4444-444444444444',
    FALSE
);

COMMIT;