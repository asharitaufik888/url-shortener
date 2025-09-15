CREATE TABLE shorten_url (
    id UUID PRIMARY KEY,
    original_url VARCHAR(255) NOT NULL,
    short_code VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    click_count BIGINT DEFAULT 0,
    user_id UUID REFERENCES users(id)
);

CREATE INDEX idx_short_code ON shorten_url(short_code);
CREATE INDEX idx_created_at ON shorten_url(created_at);
