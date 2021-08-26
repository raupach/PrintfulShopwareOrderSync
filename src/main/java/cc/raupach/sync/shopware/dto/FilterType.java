package cc.raupach.sync.shopware.dto;

public enum FilterType {
    equals,         // Exact match for the given value
    equalsAny,      // At least one exact match for a value of the given list
    contains,       // Before and after wildcard search for the given value
    range,          // For range compatible fields like numbers or dates
    not,            // Allows to negate a filter
    multi,          // Allows to combine different filters
    prefix,         // Before wildcard search for the given value
    suffix          // After wildcard search for the given value
}
