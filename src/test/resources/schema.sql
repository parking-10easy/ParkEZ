-- src/test/resources/schema.sql

CREATE TABLE public_data_page_state (
                                        id           BIGINT PRIMARY KEY,
                                        current_page INT    NOT NULL,
                                        updated_at   DATETIME NOT NULL
);

CREATE TABLE parking_lot (
                             id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
                             owner_id       BIGINT       NOT NULL,
                             name           VARCHAR(100) NOT NULL,
                             address        VARCHAR(200) NOT NULL,
                             latitude       DOUBLE,
                             longitude      DOUBLE,
                             opened_at      TIME         NOT NULL,
                             closed_at      TIME         NOT NULL,
                             price_per_hour DECIMAL(19,2),
                             description    TEXT         NOT NULL,
                             quantity       INT          NOT NULL,
                             charge_type    VARCHAR(20),
                             source_type    VARCHAR(20)  NOT NULL,
                             status         VARCHAR(20)  NOT NULL,
                             created_at     DATETIME     NOT NULL,
                             modified_at    DATETIME     NOT NULL,
                             UNIQUE KEY uk_lat_long(longitude, latitude)
);

CREATE TABLE parking_lot_image (
                                   id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
                                   parking_lot_id BIGINT       NOT NULL,
                                   image_url      VARCHAR(255) NOT NULL,
                                   created_at     DATETIME     NOT NULL,
                                   modified_at    DATETIME     NOT NULL,
                                   UNIQUE KEY uk_pl_image(parking_lot_id, image_url),
                                   FOREIGN KEY (parking_lot_id) REFERENCES parking_lot(id)
);