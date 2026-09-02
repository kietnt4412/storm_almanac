package io.stormalmanac.planner;

/** A craft or fodder application the plan wants performed, also integral. */
public record Conversion(String sourceOrSinkId, int times) {}
