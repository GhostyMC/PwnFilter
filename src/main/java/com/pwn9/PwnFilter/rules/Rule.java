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

import com.pwn9.PwnFilter.FilterTask;
import com.pwn9.PwnFilter.minecraft.PwnFilterPlugin;
import com.pwn9.PwnFilter.rules.action.Action;
import com.pwn9.PwnFilter.util.LimitedRegexCharSequence;
import com.pwn9.PwnFilter.util.LogManager;
import com.pwn9.PwnFilter.util.Patterns;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule object
 * <p/>
 * <P>Each Rule has a single match Pattern, an ArrayList of {@link com.pwn9.PwnFilter.rules.Condition}'s and an ArrayList of {@link com.pwn9.PwnFilter.rules.action.Action}'s</P>
 *
 * @author ptoal
 * @version $Id: $Id
 */
@SuppressWarnings("UnusedDeclaration")
public class Rule implements ChainEntry {
    private Pattern pattern;
    private String description = "";
    private String id = "";

    final List<Condition> conditions = new ArrayList<Condition>();
    final List<Action> actions = new ArrayList<Action>();
    public final List<String> includeEvents = new ArrayList<String>();
    public final List<String> excludeEvents = new ArrayList<String>();

        /* Constructors */

    /**
     * <p>Constructor for Rule.</p>
     */
    public Rule() {}

    /**
     * <p>Constructor for Rule.</p>
     *
     * @param matchStr a {@link java.lang.String} object.
     */
    public Rule(String matchStr) {
        this.pattern = Patterns.compilePattern(matchStr);
    }

    /**
     * <p>Constructor for Rule.</p>
     *
     * @param id a {@link java.lang.String} object.
     * @param description a {@link java.lang.String} object.
     */
    public Rule(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * <p>Getter for the field <code>pattern</code>.</p>
     *
     * @return a {@link java.util.regex.Pattern} object.
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * <p>Getter for the field <code>description</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getId() {
        return id;
    }

    /**
     * <p>Setter for the field <code>pattern</code>.</p>
     *
     * @param pattern a {@link java.lang.String} object.
     */
    public void setPattern(String pattern) {
        this.pattern = Patterns.compilePattern(pattern);
    }

    /**
     * <p>Setter for the field <code>description</code>.</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void setId(String id) {
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getPermissionList() {
        Set<String> permList = new HashSet<String>();

        for (Condition c : conditions) {
            if (c.type == Condition.CondType.permission) {
                Collections.addAll(permList, c.parameters.split("\\|"));
            }
        }

        return permList;
    }

    /* Methods */

    /**
     * {@inheritDoc}
     *
     * apply this action to the current message / event.  May trigger other bukkit events.
     */
    public void apply(FilterTask filterTask) {

        // Check if action matches the current state of the message

        if (LogManager.debugMode.compareTo(LogManager.DebugModes.high) >= 0) {
            LogManager.info("Testing Pattern: '" + pattern.toString() + "' on string: '" + filterTask.getModifiedMessage().toString()+"'");
        }

            LimitedRegexCharSequence limitedRegexCharSequence = new LimitedRegexCharSequence(filterTask.getModifiedMessage().toString(),100);
            final Matcher matcher = pattern.matcher(limitedRegexCharSequence);
        // If we don't match, return immediately with the original message
        try {
            if (!matcher.find()) return;
        } catch (RuntimeException ex) {
            LogManager.error("Regex match timed out! Regex: " + pattern.toString());
            LogManager.error("Failed string was: " + limitedRegexCharSequence);
            return;
        }

        filterTask.setPattern(pattern);
        filterTask.setRule(this);

        // If Match, log it and then check any conditions.
        filterTask.addLogMessage("|" + filterTask.getListenerName() + "| MATCH " +
                (id.isEmpty() ? "" : "(" + id + ")") +
                " <" +
                filterTask.getAuthor().getName() + "> " + filterTask.getModifiedMessage().toString());
        LogManager.getInstance().debugLow("Match String: " + matcher.group());


        for (Condition c : conditions) {
            // This checks that EVERY condition is met (conditions are AND)
            if (!c.check(filterTask)) {
                filterTask.addLogMessage("CONDITION not met <" + c.flag.toString() +
                        " " + c.type.toString() + " " + c.parameters + "> " + filterTask.getOriginalMessage());
                return;
            }

        }

        //TODO: Abstract statistics counters from mcstats
        //TODO: Make internal stats tracker on matches per hour, etc?
        if(PwnFilterPlugin.matchTracker != null) {
            PwnFilterPlugin.matchTracker.increment(); // Update Match Statistics
        }

        // If we get this far, execute the actions
        for (Action a : actions) {
            a.execute(filterTask);
        }

    }

    /**
     * <p>isValid.</p>
     *
     * @return a boolean.
     */
    public boolean isValid() {
        // Check that we have a valid pattern and at least one action
        return this.pattern != null && this.actions != null;
    }

    /**
     * <p>toString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return pattern.toString();
    }

    /**
     * <p>addCondition.</p>
     *
     * @param c a {@link com.pwn9.PwnFilter.rules.Condition} object.
     * @return a boolean.
     */
    public boolean addCondition(Condition c) {
        return c != null && conditions.add(c);
    }
    /**
     * <p>addConditions.</p>
     *
     * @param conditionList a {@link java.util.Collection} object.
     * @return a boolean.
     */
    public boolean addConditions(Collection<Condition> conditionList) {
        return conditionList != null && conditions.addAll(conditionList);
    }

    /**
     * <p>Getter for the field <code>conditions</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     * <p>add.</p>
     *
     * @param a a {@link com.pwn9.PwnFilter.rules.action.Action} object.
     * @return a boolean.
     */
    public boolean addAction(Action a) {
        return a != null && actions.add(a);
    }

    /**
     * <p>addActions.</p>
     *
     * @param actionList a {@link java.util.Collection} object.
     * @return a boolean.
     */
    public boolean addActions(Collection<Action> actionList) {
        return actionList != null && actions.addAll(actionList);
    }

    /**
     * <p>Getter for the field <code>actions</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Action> getActions() {
        return actions;
    }


}
