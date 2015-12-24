/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2013 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.PwnFilter.rules.action.targeted;

import com.pwn9.PwnFilter.FilterTask;
import com.pwn9.PwnFilter.config.FilterConfig;
import com.pwn9.PwnFilter.helpers.ChatColor;
import com.pwn9.PwnFilter.minecraft.util.FileUtil;
import com.pwn9.PwnFilter.rules.action.Action;
import com.pwn9.PwnFilter.util.LogManager;
import com.pwn9.PwnFilter.util.tags.TagRegistry;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Responds to the user with the string provided.
 *
 * @author ptoal
 * @version $Id: $Id
 */
@SuppressWarnings("UnusedDeclaration")
public class RespondFile implements Action {
    final ArrayList<String> messageStrings = new ArrayList<String>();

    /**
     * {@inheritDoc}
     */
    public void init(String s) {
        try {
            BufferedReader br = FileUtil.getBufferedReader(FilterConfig.getInstance().getTextDir(),s);
            String message;
            while ((message = br.readLine()) != null) {
                messageStrings.add(ChatColor.translateAlternateColorCodes('&', message));
            }
        } catch (FileNotFoundException ex) {
            LogManager.warn("File not found while trying to add Action: " + ex.getMessage());
            messageStrings.add("[PwnFilter] Configuration error: file not found.");
        } catch (IOException ex) {
            LogManager.warn("Error reading file: " + s);
            messageStrings.add("[PwnFilter] Error: respondfile IO.  Please notify admins.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final FilterTask filterTask) {
        final ArrayList<String> preparedMessages = new ArrayList<String>();

        for (String message : messageStrings) {
            preparedMessages.add(TagRegistry.replaceTags(message, filterTask));
        }

        filterTask.getAuthor().sendMessages(preparedMessages);

        filterTask.addLogMessage("Responded to " + filterTask.getAuthor().getName() + " with: " + preparedMessages.get(0) + "...");

    }
}

