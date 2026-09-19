package io.stormalmanac.planner;

/**
 * A craft, a shop purchase or a fodder application the plan wants performed,
 * also integral. For a purchase the id is the shop offer's, and {@code times}
 * counts purchases of the whole offered stack.
 */
public record Conversion(String sourceOrSinkId, int times) {}
