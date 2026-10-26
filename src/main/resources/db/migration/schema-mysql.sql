-- ==============================================================================
-- Hostel Room Allocation and Vacancy Registration System - MySQL Schema
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS hostel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hostel_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    student_id VARCHAR(50) UNIQUE NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    department VARCHAR(100) NULL,
    year_of_study VARCHAR(30) NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_STUDENT',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_student_id (student_id),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Hostel Blocks Table
CREATE TABLE IF NOT EXISTS hostel_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    block_name VARCHAR(50) NOT NULL UNIQUE,
    gender_type VARCHAR(20) NOT NULL,
    description TEXT NULL,
    total_floors INT NOT NULL DEFAULT 4,
    INDEX idx_block_name (block_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Rooms Table
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    block_id BIGINT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    floor INT NOT NULL,
    capacity INT NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    facilities VARCHAR(255) NULL,
    monthly_fee DECIMAL(10, 2) NOT NULL DEFAULT 4500.00,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_rooms_block FOREIGN KEY (block_id) REFERENCES hostel_blocks(id) ON DELETE RESTRICT,
    CONSTRAINT uq_block_room UNIQUE (block_id, room_number),
    INDEX idx_room_number (room_number),
    INDEX idx_room_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    preferred_block_id BIGINT NULL,
    preferred_room_type VARCHAR(50) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    special_requests TEXT NULL,
    admin_remarks TEXT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_pref_block FOREIGN KEY (preferred_block_id) REFERENCES hostel_blocks(id) ON DELETE SET NULL,
    INDEX idx_app_user (user_id),
    INDEX idx_app_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Allocations Table
CREATE TABLE IF NOT EXISTS allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_number VARCHAR(20) NOT NULL,
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    vacated_at TIMESTAMP NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    remarks TEXT NULL,
    CONSTRAINT fk_alloc_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE SET NULL,
    CONSTRAINT fk_alloc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_alloc_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    INDEX idx_alloc_room_status (room_id, status),
    INDEX idx_alloc_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
