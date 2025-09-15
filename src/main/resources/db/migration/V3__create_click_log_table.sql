CREATE TABLE click_log (
    id UUID PRIMARY KEY,
    shorten_url_id UUID NOT NULL REFERENCES shorten_url(id),
    date DATE NOT NULL,
    count BIGINT DEFAULT 0,
    UNIQUE (shorten_url_id, date)
);
