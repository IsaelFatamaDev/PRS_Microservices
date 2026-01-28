-- Payment System Database Schema
-- This script creates the required tables for the Valle Grande Payment System

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(100) PRIMARY KEY,
    organization_id VARCHAR(50),
    payment_code VARCHAR(20),
    user_id VARCHAR(50),
    water_box_id VARCHAR(50),
    payment_type VARCHAR(20),
    payment_method VARCHAR(20),
    total_amount DECIMAL(10,2),
    payment_date DATE,
    payment_status VARCHAR(20),
    external_reference VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create payment_details table
CREATE TABLE IF NOT EXISTS payment_details (
    payment_detail_id VARCHAR(100) PRIMARY KEY,
    payment_id VARCHAR(50),
    concept VARCHAR(50),
    year INTEGER,
    month INTEGER,
    amount DECIMAL(10,2),
    description TEXT,
    period_start DATE,
    period_end DATE,
    FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE CASCADE
);

-- Create receipts table
CREATE TABLE IF NOT EXISTS receipts (
    receipts_id VARCHAR(100) PRIMARY KEY,
    organization_id VARCHAR(50),
    payment_id VARCHAR(50),
    payment_detail_id VARCHAR(50),
    receipt_series VARCHAR(20),
    receipt_number VARCHAR(20),
    receipt_type VARCHAR(30),
    issue_date TIMESTAMP,
    amount DECIMAL(10,2),
    year INTEGER,
    month INTEGER,
    concept TEXT,
    customer_full_name VARCHAR(200),
    customer_document VARCHAR(30),
    pdf_generated BOOLEAN DEFAULT FALSE,
    pdf_path VARCHAR(500),
    created_at TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE CASCADE,
    FOREIGN KEY (payment_detail_id) REFERENCES payment_details(payment_detail_id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_payments_organization_id ON payments(organization_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_water_box_id ON payments(water_box_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(payment_status);
CREATE INDEX IF NOT EXISTS idx_payments_date ON payments(payment_date);

CREATE INDEX IF NOT EXISTS idx_payment_details_payment_id ON payment_details(payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_details_year_month ON payment_details(year, month);

CREATE INDEX IF NOT EXISTS idx_receipts_payment_id ON receipts(payment_id);
CREATE INDEX IF NOT EXISTS idx_receipts_payment_detail_id ON receipts(payment_detail_id);
CREATE INDEX IF NOT EXISTS idx_receipts_organization_id ON receipts(organization_id);