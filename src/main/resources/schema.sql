-- Add indexes?
BEGIN;

DROP TYPE IF EXISTS user_role_type CASCADE;
DROP TYPE IF EXISTS hotel_type CASCADE;
DROP TYPE IF EXISTS tour_type CASCADE;
DROP TYPE IF EXISTS transfer_type CASCADE;
DROP TYPE IF EXISTS status_type CASCADE;

DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS vouchers CASCADE;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE user_role_type AS ENUM (
    'ADMIN',
    'MANAGER',
    'USER'
    );

CREATE TYPE hotel_type AS ENUM (
    'ONE_STAR',
    'TWO_STAR',
    'THREE_STAR',
    'FOUR_STAR',
    'FIVE_STAR'
    );

CREATE TYPE tour_type AS ENUM (
    'HEALTH',
    'SPORTS',
    'LEISURE',
    'SAFARI',
    'WINE',
    'ECO',
    'ADVENTURE',
    'CULTURAL'
    );

CREATE TYPE transfer_type AS ENUM (
    'BUS',
    'TRAIN',
    'PLANE',
    'SHIP',
    'PRIVATE_CAR',
    'JEEPS',
    'MINIBUS',
    'ELECTRICAL_CARS'
    );

CREATE TYPE status_type AS ENUM (
    'REGISTERED',
    'PAID',
    'CANCELED'
    );

CREATE TABLE IF NOT EXISTS users (
    id uuid DEFAULT gen_random_uuid(),

    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,

    user_role user_role_type NOT NULL,

    phone_number VARCHAR(30),
    email VARCHAR(255) NOT NULL,

    balance NUMERIC(10, 2) DEFAULT 0.00 CHECK (balance >= 0),

    user_status BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),

    CONSTRAINT pk_users PRIMARY KEY (id),

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS vouchers (

    id uuid DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),

    title VARCHAR(255) NOT NULL,
    description TEXT,

    price NUMERIC(10, 2),

    voucher_tour_type tour_type,
    voucher_transfer_type transfer_type,
    voucher_hotel_type hotel_type,
    voucher_status_type status_type,

    arrival_date DATE,
    eviction_date DATE,

    user_id uuid,

    is_hot BOOLEAN DEFAULT FALSE,

    CONSTRAINT pk_vouchers PRIMARY KEY (id),

    CONSTRAINT fk_vouchers_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE SET NULL,

    CONSTRAINT chk_dates CHECK (eviction_date >= arrival_date)
    );

COMMIT;
