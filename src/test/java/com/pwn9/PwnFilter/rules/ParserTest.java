/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2013 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.PwnFilter.rules;

import com.pwn9.PwnFilter.config.FilterConfig;
import com.pwn9.PwnFilter.minecraft.PwnFilterPlugin;
import com.pwn9.PwnFilter.rules.action.Action;
import com.pwn9.PwnFilter.rules.action.RegisterActions;
import com.pwn9.PwnFilter.rules.action.core.Deny;
import com.pwn9.PwnFilter.rules.action.targeted.Respond;
import com.pwn9.PwnFilter.util.LogManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for RuleSets
 * User: ptoal
 * Date: 13-05-04
 * Time: 11:28 AM
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({PwnFilterPlugin.class})
public class ParserTest {

    RuleManager ruleManager;
    RuleChain rs;
    LogManager pwnLogger;

    PwnFilterPlugin mockPlugin;

    @Before
    public void setUp() {
        RegisterActions.all();
        //TODO: Remove this, and add a FilterEngine initialization call.
        ruleManager = RuleManager.getInstance();
        File testFile = new File(getClass().getResource("/testrules.txt").getFile());
        FilterConfig.getInstance().setRulesDir(testFile.getParentFile());
        FilterConfig.getInstance().setTextDir(testFile.getParentFile());
        Logger logger = Logger.getLogger("PwnFilter");
        pwnLogger = LogManager.getInstance(logger, new File("/tmp/"));
        //pwnLogger.start(); logger.setLevel(Level.FINEST);pwnLogger.debugMode = LogManager.DebugModes.high; // For debugging

    }

    @Test
    public void testLoadRules() {
        rs = ruleManager.getRuleChain("testrules.txt");
        assertTrue(rs.loadConfigFile());
    }

    @Test
    public void testShortcuts() {
        rs = ruleManager.getRuleChain("testrules.txt");
        rs.loadConfigFile();
        List<ChainEntry> ruleChain = rs.getChain();
        for (ChainEntry e : ruleChain) {
            if(e.toString().equals("ShortCutPattern")) {
                return;
            }
        }
        Assert.fail("Shortcut was not applied to rule!");
    }

    @Test
    // Requires actiongroup.txt in the resources folder.
    public void testActionGroupParser() throws IOException {
        RuleChain ruleChain = ruleManager.getRuleChain("actiongroup.txt");
        ruleChain.loadConfigFile();

        assertTrue(ruleChain.getActionGroups().containsKey("aGroupTest"));

        Rule rule = (Rule)ruleChain.getChain().get(0);
        List<Action> actionList = rule.getActions();

        assertTrue(actionList.remove(0) instanceof Deny);
        assertTrue(actionList.remove(0) instanceof Respond);
    }

    @Test
    // Requires conditiongroup.txt in the resources folder.
    public void testConditionGroupParser() throws IOException {
        RuleChain ruleChain = ruleManager.getRuleChain("conditiongroup.txt");
        ruleChain.loadConfigFile();

        assertTrue(ruleChain.getConditionGroups().containsKey("cGroupTest"));

        Rule rule = (Rule)ruleChain.getChain().get(0);
        List<Condition> conditionList = rule.getConditions();

        Condition cTest = conditionList.remove(0);
        assertEquals(cTest.flag, Condition.CondFlag.require);
        assertEquals(cTest.type, Condition.CondType.permission);
        assertEquals(cTest.parameters, "pwnfilter.test");

        cTest = conditionList.remove(0);
        assertEquals(cTest.flag, Condition.CondFlag.ignore);
        assertEquals(cTest.type, Condition.CondType.user);
        assertEquals(cTest.parameters, "Sage905");

        cTest = conditionList.remove(0);
        assertEquals(cTest.flag, Condition.CondFlag.ignore);
        assertEquals(cTest.type, Condition.CondType.string);
        assertEquals(cTest.parameters, "derp");

        cTest = conditionList.remove(0);
        assertEquals(cTest.flag, Condition.CondFlag.ignore);
        assertEquals(cTest.type, Condition.CondType.command);
        assertEquals(cTest.parameters, "me");
    }



}
