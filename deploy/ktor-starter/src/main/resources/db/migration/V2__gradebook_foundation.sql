-- RTIQA Gradebook Database Foundation Migration
-- Target: PostgreSQL 15+ (Supabase / PostgreSQL)
-- Migration: V2__gradebook_foundation.sql

-- 1. Academic Classes (Classroom entities owned directly by an organization tenant)
CREATE TABLE IF NOT EXISTS academic_classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    academic_year VARCHAR(100),
    term VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Class Students (Class enrollment/membership mapping students to classes)
-- Note: Student display names are strictly sourced from profiles.name to preserve single-source identity.
CREATE TABLE IF NOT EXISTS class_students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL REFERENCES academic_classes(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    student_number VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_class_student UNIQUE (class_id, student_id)
);

-- 3. Gradebook Assessments (Evaluations belonging to an academic class)
-- Note: Weight is stored canonically as 0..1 (e.g. 0.20 for 20%), or NULL if unweighted.
CREATE TABLE IF NOT EXISTS gradebook_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL REFERENCES academic_classes(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    max_score NUMERIC NOT NULL CHECK (max_score > 0),
    weight NUMERIC NULL CHECK (weight IS NULL OR (weight >= 0 AND weight <= 1)),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Gradebook Scores (Granular student scores per assessment)
-- Note: Cross-table constraint (score <= assessment.max_score) cannot be enforced via a simple
-- SQL CHECK constraint; it is enforced in application service/repository logic.
-- Non-negativity (score >= 0) is enforced at the database level.
-- organization_id and class_id are intentionally omitted to avoid denormalization (resolved via assessment -> class).
CREATE TABLE IF NOT EXISTS gradebook_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID NOT NULL REFERENCES gradebook_assessments(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    score NUMERIC NULL CHECK (score IS NULL OR score >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_assessment_student_score UNIQUE (assessment_id, student_id)
);

-- Performance & Foreign Key Lookup Indexes
CREATE INDEX IF NOT EXISTS idx_academic_classes_org_id ON academic_classes(organization_id);
CREATE INDEX IF NOT EXISTS idx_class_students_class_id ON class_students(class_id);
CREATE INDEX IF NOT EXISTS idx_class_students_student_id ON class_students(student_id);
CREATE INDEX IF NOT EXISTS idx_gradebook_assessments_class_id ON gradebook_assessments(class_id);
CREATE INDEX IF NOT EXISTS idx_gradebook_scores_assessment_id ON gradebook_scores(assessment_id);
CREATE INDEX IF NOT EXISTS idx_gradebook_scores_student_id ON gradebook_scores(student_id);
