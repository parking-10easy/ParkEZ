CREATE TABLE IF NOT EXISTS public_data_page_state (
    id            BIGINT       NOT NULL,
    current_page  INT          NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (id)
    );