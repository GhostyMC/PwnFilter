/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2013 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.PwnFilter.util;

import javax.annotation.Nonnull;

/**
 * Create a Timed Regex match.
 * User: ptoal
 * Date: 13-07-26
 * Time: 4:35 PM
 *
 * @author ptoal
 * @version $Id: $Id
 */

/* NOTE: The goal here is to create a matcher that won't run forever.
 Here's how this works:
 1. The TimeoutRegexCharSequence has a timeout set in it of the system time + some interval.
 2. On every charAt access, we check to see if we're past that time.
 3. If so, then throw an exception, which will halt the regex processing, and notify
 4. the caller.  In PwnFilter, we can then check for this exception, disable the rule, and log the offending regex and
 string.
*/
public class LimitedRegexCharSequence implements CharSequence {

    private final CharSequence inner;

    private final int timeoutMillis;

    private final long timeoutTime;

    private long accessCount;

    /**
     * <p>Constructor for LimitedRegexCharSequence.</p>
     *
     * @param inner a {@link java.lang.CharSequence} object.
     * @param timeoutMillis a int.
     */
    public LimitedRegexCharSequence(CharSequence inner, int timeoutMillis)  {
        super();
        if ( inner == null ) {
            throw new NullPointerException("CharSequence must not be null");
        }
        this.inner = inner;
        this.timeoutMillis = timeoutMillis;
        timeoutTime = System.currentTimeMillis() + timeoutMillis;
        accessCount = 0;
    }

    /** {@inheritDoc} */
    public char charAt(int index) {
        accessCount++ ;
        if (System.currentTimeMillis() > timeoutTime) {
            throw new RuntimeException("Timeout occurred after " + timeoutMillis + "ms");
        }
        return inner.charAt(index);
    }

    /**
     * <p>length.</p>
     *
     * @return a int.
     */
    public int length() {
        return inner.length();
    }

    /** {@inheritDoc} */
    public CharSequence subSequence(int start, int end) {
        return new LimitedRegexCharSequence(inner.subSequence(start, end), timeoutMillis);
    }

    /**
     * <p>Getter for the field <code>accessCount</code>.</p>
     *
     * @return a long.
     */
    public long getAccessCount() {
        return accessCount;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String toString() {
        return inner.toString();
    }
}
