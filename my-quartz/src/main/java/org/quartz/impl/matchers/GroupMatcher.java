package org.quartz.impl.matchers;

public class GroupMatcher<T extends Key<?>> extends StringMatcher<T> {

    private static final long serialVersionUID = -3275767650469343849L;

    protected GroupMatcher(String compareTo, StringOperatorName compareWith) {
        super(compareTo, compareWith);
    }

    /**
     * Create a GroupMatcher that matches groups equaling the given string.
     */
    public static <T extends Key<T>> GroupMatcher<T> groupEquals(String compareTo) {
        return new GroupMatcher<T>(compareTo, StringOperatorName.EQUALS);
    }

    /**
     * Create a GroupMatcher that matches job groups equaling the given string.
     */
    public static GroupMatcher<JobKey> jobGroupEquals(String compareTo) {
        return GroupMatcher.groupEquals(compareTo);
    }

    /**
     * Create a GroupMatcher that matches trigger groups equaling the given string.
     */
    public static GroupMatcher<TriggerKey> triggerGroupEquals(String compareTo) {
        return GroupMatcher.groupEquals(compareTo);
    }

    /**
     * Create a GroupMatcher that matches groups starting with the given string.
     */
    public static <T extends Key<T>> GroupMatcher<T> groupStartsWith(String compareTo) {
        return new GroupMatcher<T>(compareTo, StringOperatorName.STARTS_WITH);
    }

    /**
     * Create a GroupMatcher that matches job groups starting with the given string.
     */
    public static GroupMatcher<JobKey> jobGroupStartsWith(String compareTo) {
        return GroupMatcher.groupStartsWith(compareTo);
    }

    /**
     * Create a GroupMatcher that matches trigger groups starting with the given string.
     */
    public static GroupMatcher<TriggerKey> triggerGroupStartsWith(String compareTo) {
        return GroupMatcher.groupStartsWith(compareTo);
    }

    /**
     * Create a GroupMatcher that matches groups ending with the given string.
     */
    public static <T extends Key<T>> GroupMatcher<T> groupEndsWith(String compareTo) {
        return new GroupMatcher<T>(compareTo, StringOperatorName.ENDS_WITH);
    }

    /**
     * Create a GroupMatcher that matches job groups ending with the given string.
     */
    public static GroupMatcher<JobKey> jobGroupEndsWith(String compareTo) {
        return GroupMatcher.groupEndsWith(compareTo);
    }

    /**
     * Create a GroupMatcher that matches trigger groups ending with the given string.
     */
    public static GroupMatcher<TriggerKey> triggerGroupEndsWith(String compareTo) {
        return GroupMatcher.groupEndsWith(compareTo);
    }

    /**
     * Create a GroupMatcher that matches groups containing the given string.
     */
    public static <T extends Key<T>> GroupMatcher<T> groupContains(String compareTo) {
        return new GroupMatcher<T>(compareTo, StringOperatorName.CONTAINS);
    }

    /**
     * Create a GroupMatcher that matches job groups containing the given string.
     */
    public static GroupMatcher<JobKey> jobGroupContains(String compareTo) {
        return GroupMatcher.groupContains(compareTo);
    }

    /**
     * Create a GroupMatcher that matches trigger groups containing the given string.
     */
    public static GroupMatcher<TriggerKey> triggerGroupContains(String compareTo) {
        return GroupMatcher.groupContains(compareTo);
    }

    @Override
    protected String getValue(T key) {
        return key.getGroup();
    }

}

