/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2015 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.PwnFilter.minecraft.listener;

import static junit.framework.Assert.assertTrue;
import com.pwn9.PwnFilter.TestMinecraftAPI;
import com.pwn9.PwnFilter.config.FilterConfig;
import com.pwn9.PwnFilter.config.SpongeConfig;
import com.pwn9.PwnFilter.minecraft.api.MinecraftAPI;
import com.pwn9.PwnFilter.minecraft.api.MinecraftServer;
import com.pwn9.PwnFilter.rules.RuleChain;
import com.pwn9.PwnFilter.rules.action.RegisterActions;
import com.pwn9.PwnFilter.util.LogManager;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.sink.MessageSinkFactory;

import java.io.File;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Test the Bukkit Built-in Chat Filter Listener
 *
 * This is more of a smoke test than a Unit test.  It's difficult to test the
 * listener without testing a lot of the other components upon which it depends.
 *
 * Created by ptoal on 15-09-10.
 */

@RunWith(EasyMockRunner.class)
public class PwnFilterPlayerListenerTest {

    @Mock
    Player mockPlayer;

    MessageSinkEvent.Chat chatEvent;
    Configuration testConfig;
    final File resourcesDir = new File(getClass().getResource("/config.yml").getFile()).getParentFile();
    final PwnFilterPlayerListener playerListener = new PwnFilterPlayerListener();
    final MinecraftAPI minecraftAPI = new TestMinecraftAPI();



    @Before
    public void setUp() {
        RegisterActions.all();
        LogManager.getInstance(Logger.getAnonymousLogger(), new File("/pwnfiltertest.log"));
        File rulesDir = new File(getClass().getResource("/rules").getFile());
        FilterConfig.getInstance().setRulesDir(rulesDir);
        testConfig = YamlConfiguration.loadConfiguration(new File(getClass().getResource("/config.yml").getFile()));
        SpongeConfig.loadConfiguration(testConfig, resourcesDir);
        MinecraftServer.setAPI(minecraftAPI);
        SpongeConfig.setGlobalMute(false); // To ensure it gets reset between tests.
    }

    @Test
    public void testBasicFunctionWorks() throws Exception {
        RuleChain ruleChain = new RuleChain("blank.txt");
        ruleChain.loadConfigFile();

        String input = "Test Chat Message";
        chatEvent = new AsyncPlayerChatEvent(true, mockPlayer, input, new HashSet<Player>());
        playerListener.setRuleChain(ruleChain);
        playerListener.onPlayerChat(chatEvent);
        assertEquals(chatEvent.getMessage(), input);
    }

    @Test
    public void testExecutesRules() throws Exception {
        RuleChain ruleChain = new RuleChain("replace.txt");
        ruleChain.loadConfigFile();

        String input = "Test replaceme Message";
        chatEvent = new AsyncPlayerChatEvent(true, mockPlayer, input, new HashSet<Player>());
        playerListener.setRuleChain(ruleChain);
        playerListener.onPlayerChat(chatEvent);
        assertEquals(chatEvent.getMessage(), "Test PASS Message");
    }

    @Test
    public void testGlobalMuteCancelsMessage() throws Exception {
        RuleChain ruleChain = new RuleChain("blank.txt");
        ruleChain.loadConfigFile();

        String input = "Test Message";
        SpongeConfig.setGlobalMute(true);
        chatEvent = new AsyncPlayerChatEvent(true, mockPlayer, input, new HashSet<Player>());
        playerListener.setRuleChain(ruleChain);
        playerListener.onPlayerChat(chatEvent);
        assertTrue(chatEvent.isCancelled());
    }

    @Test
    public void testDecolorMessage() throws Exception {
        RuleChain ruleChain = new RuleChain("blank.txt");
        ruleChain.loadConfigFile();

        String input = "Test&4 Message";
        testConfig.set("decolor", true);
        SpongeConfig.loadConfiguration(testConfig, resourcesDir);

        chatEvent = new AsyncPlayerChatEvent(true, mockPlayer, input, new HashSet<Player>());
        playerListener.setRuleChain(ruleChain);
        playerListener.onPlayerChat(chatEvent);
        assertEquals(chatEvent.getMessage(), "Test Message");
        testConfig.set("decolor", false);
    }

    // https://github.com/Pwn9/PwnFilter/issues/13
    @Test
    public void testLowerMessage() throws Exception {
        RuleChain ruleChain = new RuleChain("actionTests.txt");
        ruleChain.loadConfigFile();

        String input = "HEY! THIS SHOULD ALL GET LOWERED.";
        SpongeConfig.loadConfiguration(testConfig, resourcesDir);

        chatEvent = new AsyncPlayerChatEvent(true, mockPlayer, input, new HashSet<Player>());
        playerListener.setRuleChain(ruleChain);
        playerListener.onPlayerChat(chatEvent);
        assertTrue(!chatEvent.isCancelled());
        assertEquals(chatEvent.getMessage(), "HEY! this should all get lowered.");
    }



}