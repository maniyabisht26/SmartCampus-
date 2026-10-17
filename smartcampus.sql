USE smartcampus;

CREATE TABLE IF NOT EXISTS students (
    student_id    INT AUTO_INCREMENT PRIMARY KEY,
    roll_number   VARCHAR(20) NOT NULL UNIQUE,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    phone         VARCHAR(10) NOT NULL,
    department    VARCHAR(50) NOT NULL,
    year_of_study INT NOT NULL,
    registered_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);