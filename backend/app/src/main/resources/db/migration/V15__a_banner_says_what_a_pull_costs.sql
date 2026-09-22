-- A banner can now say which item a pull is paid in and how many of it one pull
-- costs.
--
-- FOUND BY: N28 in TRACKER.md, open since phase 5 closed. IncomeModel — "how
-- much pull currency will this account have by the date she arrives" — is an
-- interface with no implementation, and could not have one, because neither
-- BannerModel nor this table declared either half of a price. The odds were
-- modelled to three decimals beside a question nobody could ask.
--
-- Punishing: Gray Raven prices every one of its four pools at 250 tickets for
-- one and 2 500 for ten, so there is no multi-pull discount to express and this
-- is one price rather than a list. Each pool has its OWN ticket, which is why
-- the currency hangs off the banner rather than off the game: a column on
-- gamedata.game would have had to pick one of the four.
--
-- Both columns are nullable and NULL means UNSTATED rather than free. The
-- banner already stored here was read for its rates in a sitting that did not
-- record a price, and it has to read back as what it claimed; there is no
-- default to fall back on the way the day boundary falls back to midnight, so
-- BannerModel.pricedPull() refuses instead of inventing a zero.
ALTER TABLE gamedata.banner
    ADD COLUMN pull_currency_id BIGINT,
    ADD COLUMN pull_cost        INT;

-- The currency is an item of the same version, on the composite key every
-- reference in this schema uses. A pull priced in another version's item is the
-- failure ADR 0008 exists to make impossible rather than unlikely.
ALTER TABLE gamedata.banner
    ADD CONSTRAINT banner_pull_currency_fk FOREIGN KEY (version_id, pull_currency_id)
        REFERENCES gamedata.item (version_id, id);

-- Both or neither, and a stated price is at least one. A currency with no
-- number is not half a price, it is a row the reader would have to invent a
-- number for; a number with no currency is not a price at all. Mirrors
-- PullPrice's constructor.
ALTER TABLE gamedata.banner
    ADD CONSTRAINT banner_pull_price_is_whole CHECK (
        (pull_currency_id IS NULL AND pull_cost IS NULL)
        OR (pull_currency_id IS NOT NULL AND pull_cost IS NOT NULL AND pull_cost >= 1)
    );
