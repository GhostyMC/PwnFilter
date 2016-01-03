/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2015 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.PwnFilter.minecraft;

import com.google.common.collect.MapMaker;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple Singleton to track Player Death Messages
 * Created by ptoal on 15-09-08.
 */
public class DeathMessages {
    public static final ConcurrentMap<UUID, String> killedPlayers = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();

    /**
     * <p>addKilledPlayer.</p>
     *
     * @param p a {@link UUID} object.
     * @param message a {@link String} object.
     */
    public static void addKilledPlayer(UUID p, String message) {
        killedPlayers.put(p, message);
    }
}
