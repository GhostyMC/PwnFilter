/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2015 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.PwnFilter.util.tags;

import com.pwn9.PwnFilter.FilterTask;
import com.pwn9.PwnFilter.api.FilterClient;
import com.pwn9.PwnFilter.api.MessageAuthor;
import com.pwn9.PwnFilter.rules.RuleChain;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Tests for TagRegistry
 * Created by ptoal on 15-09-07.
 */
public class TagRegistryTest {

    MessageAuthor testAuthor;
    FilterClient testClient;


    @Before
    public void setUp() {
        testAuthor = new MessageAuthor() {
            final UUID testId = UUID.randomUUID();

            public boolean hasPermission(String permString) {
                return false;
            }

            @Nonnull
            @Override
            public String getName() {
                return "";
            }

            @Nonnull
            @Override
            public UUID getID() {
                return testId;
            }

            @Override
            public void sendMessage(String message) {

            }

            @Override
            public void sendMessages(List<String> messages) {

            }
        };

        testClient = new FilterClient() {
            @Override
            public String getShortName() {
                return "TEST";
            }

            @Override
            public RuleChain getRuleChain() {
                return null;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public void activate() {

            }

            @Override
            public void shutdown() {

            }
        };

    }

    @Test
    public void testUntaggedStringIsUnmodified() throws Exception {

        FilterTask testState = new FilterTask("TestString", testAuthor, testClient);

        String input = "This is a test";
        String result = TagRegistry.replaceTags(input, testState);
        Assert.assertEquals(input, result);
    }

    @Test
    public void testUnmatchedTagIsUnmodified() throws Exception {
        FilterTask testState = new FilterTask("TestString", testAuthor, testClient);

        String input = "This is a %test%";
        String result = TagRegistry.replaceTags(input, testState);
        Assert.assertEquals(input, result);
    }

    @Test
    public void testStaticTagIsReplaced() throws Exception {
        FilterTask testState = new FilterTask("TestString", testAuthor, testClient);

        TagRegistry.addTag("test", new Tag() {
            @Override
            public String getValue(FilterTask filterTask) {
                return "foo";
            }
        });

        String input = "This is a %test%";
        String result = TagRegistry.replaceTags(input, testState);
        Assert.assertEquals("This is a foo", result);

    }
}