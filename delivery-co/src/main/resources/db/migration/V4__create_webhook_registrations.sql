-- Create webhook_registrations table for storing Store callback URLs
CREATE TABLE webhook_registrations (
  id BIGSERIAL PRIMARY KEY,
  event VARCHAR(255) NOT NULL UNIQUE,
  callback_url VARCHAR(500) NOT NULL,
  registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast event lookup
CREATE INDEX idx_webhook_event ON webhook_registrations(event);
