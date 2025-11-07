-- PostgreSQL initialization script for microservices
-- Creates all required databases when container first starts
-- This script runs automatically via docker-entrypoint-initdb.d

-- Create delivery service database
CREATE DATABASE delivery_db;

-- Create email service database
CREATE DATABASE email_db;

-- Create warehouse service database
CREATE DATABASE warehouse;

-- Note: store_db is created automatically via POSTGRES_DB environment variable
