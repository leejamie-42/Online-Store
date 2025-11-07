CREATE TABLE webhook_registrations (
    id BIGSERIAL PRIMARY KEY,
    event VARCHAR(100) NOT NULL,
    callback_url VARCHAR(500) NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_event ON webhook_registrations(event);

