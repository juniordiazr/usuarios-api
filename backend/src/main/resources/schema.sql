CREATE TABLE IF NOT EXISTS users (
    id          UUID          NOT NULL PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    created     TIMESTAMP     NOT NULL,
    modified    TIMESTAMP     NOT NULL,
    last_login  TIMESTAMP     NOT NULL,
    token       VARCHAR(512)  NOT NULL,
    is_active   BOOLEAN       NOT NULL
);

CREATE TABLE IF NOT EXISTS phones (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number  VARCHAR(50),
    city_code     VARCHAR(10),
    country_code  VARCHAR(10),
    user_id       UUID NOT NULL,
    CONSTRAINT fk_phones_user FOREIGN KEY (user_id) REFERENCES users(id)
);
