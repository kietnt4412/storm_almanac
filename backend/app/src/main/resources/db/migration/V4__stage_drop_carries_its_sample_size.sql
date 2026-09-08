-- A drop yield is a mean per run, and until now the schema could not say how
-- many runs it was a mean of.
--
-- FOUND BY: comparing this project's stage ranking against a published community
-- guide (docs/benchmarks/reverse-1999-community-answers.md). Every disagreement
-- over 25% had the same shape — a mean over a hundred runs outranking a mean
-- over several thousand, because to the model they are the same number. The
-- upstream publishes the sample size per stage, from 105 runs to 41 212, and the
-- adapter was dividing by it and discarding it, because Drop had nowhere to put
-- it and neither did this table.
--
-- 0 means DECLARED, not "measured over nothing". A fixed-reward stage states its
-- reward; an upstream may publish a rate with no provenance at all. Both take
-- the value at face value, which is what a sample size of zero has to mean for
-- the pre-existing rows this migration backfills: every one of them was written
-- before anything could record a sample, so "declared" is the only claim about
-- them that is true. A row asserting a measurement over zero runs is rejected by
-- the CHECK below rather than defaulted, because that is a false claim and not a
-- missing one -- see the same refusal in CanonicalBundleParser.
--
-- The column is NOT NULL with a default rather than nullable. NULL and 0 would
-- mean the same thing here, and two spellings of one meaning is how a query
-- comes to disagree with a record.
ALTER TABLE gamedata.stage_drop
    ADD COLUMN sampled_runs BIGINT NOT NULL DEFAULT 0;

ALTER TABLE gamedata.stage_drop
    ADD CONSTRAINT stage_drop_sampled_runs_non_negative CHECK (sampled_runs >= 0);

COMMENT ON COLUMN gamedata.stage_drop.sampled_runs IS
    'Runs behind expected_yield; 0 means the data declares the yield rather than measuring it.';
