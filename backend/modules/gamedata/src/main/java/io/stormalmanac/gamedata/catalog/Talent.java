package io.stormalmanac.gamedata.catalog;

/** A passive, gated behind a state on the entity's upgrade graph. */
public record Talent(String id, String displayName, String unlockCondition, String effect) {}
