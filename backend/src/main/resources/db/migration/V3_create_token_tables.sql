-- =====================================================================
-- V3: Tạo bảng refresh_tokens và invalidated_tokens phục vụ xác thực JWT.
-- =====================================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(512) NOT NULL,
    expiry_date DATETIME(6)  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_refresh_token_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_token_token ON refresh_tokens (token);

CREATE TABLE IF NOT EXISTS invalidated_tokens (
    id          VARCHAR(255) NOT NULL,
    expiry_time DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

