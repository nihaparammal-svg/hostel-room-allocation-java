-- ==============================================================================
-- Hostel Room Allocation and Vacancy Registration System - PostgreSQL Schema
-- Compatible with Render Managed PostgreSQL
-- ==============================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    student_id VARCHAR(50) UNIQUE NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    department VARCHAR(100) NULL,
    year_of_study VARCHAR(30) NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_STUDENT',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_student_id ON users(student_id);
CREATE INDEX IF NOT EXISTS idx_user_role ON users(role);

-- 2. Hostel Blocks Table
CREATE TABLE IF NOT EXISTS hostel_blocks (
    id BIGSERIAL PRIMARY KEY,
    block_name VARCHAR(50) NOT NULL UNIQUE,
    gender_type VARCHAR(20) NOT NULL,
    description TEXT NULL,
    total_floors INT NOT NULL DEFAULT 4
);

CREATE INDEX IF NOT EXISTS idx_block_name ON hostel_blocks(block_name);

-- 3. Rooms Table
CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    block_id BIGINT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    floor INT NOT NULL,
    capacity INT NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    facilities VARCHAR(255) NULL,
    monthly_fee DECIMAL(10, 2) NOT NULL DEFAULT 4500.00,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_rooms_block FOREIGN KEY (block_id) REFERENCES hostel_blocks(id) ON DELETE RESTRICT,
    CONSTRAINT uq_block_room UNIQUE (block_id, room_number)
);

CREATE INDEX IF NOT EXISTS idx_room_number ON rooms(room_number);
CREATE INDEX IF NOT EXISTS idx_room_status ON rooms(status);

-- 4. Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    preferred_block_id BIGINT NULL,
    preferred_room_type VARCHAR(50) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    special_requests TEXT NULL,
    admin_remarks TEXT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_pref_block FOREIGN KEY (preferred_block_id) REFERENCES hostel_blocks(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_app_user ON applications(user_id);
CREATE INDEX IF NOT EXISTS idx_app_status ON applications(status);

-- 5. Allocations Table
CREATE TABLE IF NOT EXISTS allocations (
    id BIGSERIAL PRIMARY KEY,
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
    CONSTRAINT fk_alloc_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_alloc_room_status ON allocations(room_id, status);
CREATE INDEX IF NOT EXISTS idx_alloc_user ON allocations(user_id);

-- 6. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notif_user_read ON notifications(user_id, is_read);
