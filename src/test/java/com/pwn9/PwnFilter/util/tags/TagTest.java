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
import com.pwn9.PwnFilter.minecraft.tag.PlayerTag;
import com.pwn9.PwnFilter.rules.RuleChain;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Test Tags
 * Created by ptoal on 15-09-07.
 */
public class TagTest {
    FilterClient testClient;
    MessageAuthor testAuthor;

    @Before
    public void setUp() throws Exception {
        testClient = new FilterClient() {
            @Override
            public String getShortName() {
                return "TESTCLIENT";
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

        testAuthor = new MessageAuthor() {
            private final UUID testUUID = UUID.randomUUID();

            @Override
            public boolean hasPermission(String permString) {
                return false;
            }

            @Nonnull
            @Override
            public String getName() {
                return "TESTPLAYER";
            }

            @Nonnull
            @Override
            public UUID getID() {
                return testUUID;
            }

            @Override
            public void sendMessage(String message) {

            }

            @Override
            public void sendMessages(List<String> messages) {

            }
        };

    }

    @Test
    public void testBuiltinTags() throws Exception {
        String input = "Test %player% tag";
        FilterTask testState = new FilterTask(input, testAuthor , testClient );
        TagRegistry.addTag("player",new PlayerTag());
        Assert.assertEquals(TagRegistry.replaceTags(input, testState), "Test TESTPLAYER tag");

    }
}