-- Run this in MySQL Workbench against your LOCAL MySQL for testing,
-- and again against your Railway MySQL database before going live
-- (Railway's "Data" tab has a Query console you can paste this into).

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'MEMBER',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS books;

CREATE TABLE books (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(150) NOT NULL,
    author            VARCHAR(100) NOT NULL,
    isbn              VARCHAR(20),
    total_copies      INT NOT NULL DEFAULT 1,
    available_copies  INT NOT NULL DEFAULT 1
);

CREATE TABLE loans (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    book_id      INT NOT NULL,
    user_id      INT NOT NULL,
    borrow_date  DATE NOT NULL,
    due_date     DATE NOT NULL,
    return_date  DATE NULL,
    fine         DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    status       VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO books (title, author, isbn, total_copies, available_copies) VALUES
('Clean Code', 'Robert C. Martin', '9780132350884', 3, 3),
('Effective Java', 'Joshua Bloch', '9780134685991', 2, 2);

-- Register an account through the site first, then run this to make
-- yourself an admin (unlocks Inventory / Admin Dashboard / Users):
-- UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
