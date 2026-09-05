-- RTIQA Phase 2C Initial Production Schema for Supabase PostgreSQL
-- Deployment Target: Supabase PostgreSQL (PostgreSQL 15+)
-- RLS Policy Enforcement: Intentionally deferred to Phase 2F following Auth & Tenancy wiring.

-- 1. Organizations (Tenants)
CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'SCHOOL' CHECK (type IN ('SCHOOL', 'UNIVERSITY', 'INSTITUTE', 'ACADEMY')),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. User Profiles (Global identity mapped 1:1 to Supabase auth.users)
-- Note: An email copy is stored here to enable high-performance application queries
-- without requiring cross-schema joins to auth.users.
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    avatar_url TEXT,
    level_xp INT NOT NULL DEFAULT 0 CHECK (level_xp >= 0),
    streak_days INT NOT NULL DEFAULT 0 CHECK (streak_days >= 0),
    is_offline_mode_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Organization Memberships (Multi-tenancy & Roles)
-- Note on SUPER_ADMIN: As defined in the RTIQA enterprise domain, SUPER_ADMIN is a platform-wide
-- governance role across all tenants, not an individual organization membership role. Platform
-- admins are evaluated via profile-level or auth-level attributes, not organization_members.
CREATE TABLE IF NOT EXISTS organization_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ORG_ADMIN', 'PRINCIPAL', 'VICE_PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT', 'STAFF')),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'INVITATION_PENDING')),
    department VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_org_user_membership UNIQUE (organization_id, user_id)
);

-- 4. Courses (Shared educational curriculum owned by an organization)
-- User progress metrics (completed_modules, progress_percent) are excluded from the shared course entity.
-- Total lessons count is dynamically derived via COUNT(lessons.id) to prevent materialized count drift.
CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    level VARCHAR(50) NOT NULL DEFAULT 'مبتدئ',
    duration_minutes INT NOT NULL DEFAULT 0 CHECK (duration_minutes >= 0),
    icon_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. Lessons (Ordered curriculum modules within a course)
CREATE TABLE IF NOT EXISTS lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    module_order INT NOT NULL CHECK (module_order > 0),
    audio_url TEXT,
    estimated_minutes INT NOT NULL DEFAULT 15 CHECK (estimated_minutes > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_course_module_order UNIQUE (course_id, module_order)
);

-- 6. Enrollments (User-specific course engagement and metrics)
CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'enrolled',
    progress_percent REAL NOT NULL DEFAULT 0.0 CHECK (progress_percent >= 0.0 AND progress_percent <= 100.0),
    completed_lessons INT NOT NULL DEFAULT 0 CHECK (completed_lessons >= 0),
    completed_at TIMESTAMPTZ,
    last_accessed_at TIMESTAMPTZ,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_course_enrollment UNIQUE (user_id, course_id)
);

-- 7. Progress Records (Granular lesson completion per user)
-- Note: course_id is intentionally omitted to maintain strict normalization; lesson_id
-- is the single authority for course association through lessons.course_id.
CREATE TABLE IF NOT EXISTS progress_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    score INT NOT NULL DEFAULT 0 CHECK (score >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_lesson_progress UNIQUE (user_id, lesson_id)
);

-- Performance & Foreign Key Lookup Indexes
CREATE INDEX IF NOT EXISTS idx_org_members_org_id ON organization_members(organization_id);
CREATE INDEX IF NOT EXISTS idx_org_members_user_id ON organization_members(user_id);
CREATE INDEX IF NOT EXISTS idx_courses_org_id ON courses(organization_id);
CREATE INDEX IF NOT EXISTS idx_lessons_course_order ON lessons(course_id, module_order);
CREATE INDEX IF NOT EXISTS idx_enrollments_user_id ON enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_progress_user_lesson ON progress_records(user_id, lesson_id);
