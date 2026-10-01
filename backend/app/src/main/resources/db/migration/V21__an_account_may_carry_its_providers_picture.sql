-- An account may carry the picture its provider shows for it (the maintainer,
-- 2026-10-01: the top bar's account chip shows the reader's Google photo).
--
-- A LINK, NOT AN IMAGE. The provider hosts the picture; this stores where.
--   Nothing is fetched or copied, so no game asset and no stranger's photo ever
--   sits in this database, and a reader who changes their photo at Google sees
--   it here on their next sign-in.
--
-- REFRESHED ON EVERY SIGN-IN, LIKE display_name. The provider is the authority,
--   so a sign-in that sends no picture clears it rather than keeping a stale
--   one. NULL is "no picture", and the page shows initials.
--
-- ONLY https. SignIn drops anything else before it gets here; the CHECK is the
--   database agreeing, so a page can put the value in an <img> without asking.

ALTER TABLE identity.account
    ADD COLUMN picture_url TEXT,
    ADD CONSTRAINT account_picture_url_is_https CHECK (picture_url IS NULL OR picture_url LIKE 'https://%');
