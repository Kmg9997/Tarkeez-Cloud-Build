-- مِعيار: مخطط أولي لـ PostgreSQL/Supabase. لا ينشر خدمات تلقائياً.
create table if not exists public.references_catalog (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  publisher text not null,
  source_url text not null check (source_url ~ '^https://'),
  section_locator text not null,
  checked_at timestamptz,
  rights_note text not null default '',
  created_at timestamptz not null default now()
);
create table if not exists public.lessons (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  body text not null,
  reference_id uuid not null references public.references_catalog(id),
  review_status text not null default 'draft' check (review_status in ('draft','needs_review','approved')),
  reviewed_by text,
  reviewed_at timestamptz,
  published_at timestamptz,
  check (review_status <> 'approved' or (reviewed_by is not null and reviewed_at is not null))
);
create table if not exists public.questions (
  id uuid primary key default gen_random_uuid(),
  lesson_id uuid not null references public.lessons(id),
  prompt text not null,
  choices jsonb not null,
  correct_index integer not null check (correct_index between 0 and 3),
  explanation text not null,
  review_status text not null default 'draft' check (review_status in ('draft','needs_review','approved')),
  reviewed_by text,
  reviewed_at timestamptz,
  check (review_status <> 'approved' or (reviewed_by is not null and reviewed_at is not null))
);
-- AI budget and quota are SERVER-ONLY; never grant client write access.
create table if not exists public.ai_usage (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  request_id uuid not null unique,
  month_key date not null,
  reserved_sar numeric(12,6) not null default 0,
  actual_sar numeric(12,6),
  status text not null check (status in ('reserved','settled','refunded')),
  created_at timestamptz not null default now()
);
create table if not exists public.ai_budget (
  month_key date primary key,
  limit_sar numeric(12,6) not null check (limit_sar >= 0),
  reserved_sar numeric(12,6) not null default 0,
  spent_sar numeric(12,6) not null default 0,
  enabled boolean not null default false
);
alter table public.references_catalog enable row level security;
alter table public.lessons enable row level security;
alter table public.questions enable row level security;
alter table public.ai_usage enable row level security;
alter table public.ai_budget enable row level security;
-- Public users may only read approved lessons/questions. Source metadata is visible
-- only when referenced by a published approved lesson.
create policy "read approved lessons" on public.lessons for select to anon, authenticated
  using (review_status = 'approved' and published_at is not null);
create policy "read approved questions" on public.questions for select to anon, authenticated
  using (review_status = 'approved' and exists (
    select 1 from public.lessons l where l.id = lesson_id
    and l.review_status = 'approved' and l.published_at is not null
  ));
create policy "read published references" on public.references_catalog for select to anon, authenticated
  using (exists (
    select 1 from public.lessons l where l.reference_id = id
    and l.review_status = 'approved' and l.published_at is not null
  ));
-- No client policies for ai_usage/ai_budget. Implement atomic budget reservation
-- in a server-side transaction with row locks before connecting a paid AI provider.
-- Do not expose service-role credentials to Android.
