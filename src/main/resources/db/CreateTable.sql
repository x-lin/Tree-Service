CREATE TABLE IF NOT EXISTS edge (
    from_id INT NOT NULL,
    to_id INT NOT NULL,
    PRIMARY KEY (from_id, to_id)
);
CREATE INDEX IF NOT EXISTS from_id_index ON edge (from_id);
CREATE INDEX IF NOT EXISTS to_id_index ON edge (to_id);
