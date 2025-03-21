USE fraud_detection;

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cc_num BIGINT NOT NULL,
    amt DECIMAL(10,2) NOT NULL,
    zip VARCHAR(10),
    lat FLOAT NOT NULL,
    `long` FLOAT NOT NULL,
    city_pop INT NOT NULL,
    unix_time BIGINT NOT NULL,
    merch_lat FLOAT NOT NULL,
    merch_long FLOAT NOT NULL,
    INDEX idx_cc_num (cc_num),
    INDEX idx_unix_time (unix_time)
);

CREATE TABLE IF NOT EXISTS fraud_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cc_num BIGINT NOT NULL,
    reason TEXT NOT NULL,
    detected_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_data JSON,
    INDEX idx_cc_num (cc_num)
);