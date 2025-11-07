-- =====================================================
-- Migration: V1 - Create Users Table
-- Description: Creates the users table for authentication
-- Author: System
-- Date: 2025-10-24
-- =====================================================

-- Create users table
-- Maps to: com.comp5348.store.model.auth.User
--

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create unique indexes for users table
-- Ensures email uniqueness for authentication
CREATE UNIQUE INDEX idx_user_email ON users(email);

-- Ensures username uniqueness
CREATE UNIQUE INDEX idx_user_name ON users(name);
