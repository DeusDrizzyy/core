package com.minecraft.net.nast.core.utils;

import com.google.common.collect.ImmutableList;

public class BlockedCommands {
    public static final ImmutableList<String> BLOCKED_COMMANDS = ImmutableList.of(
            "/plugins",
            "/pl",
            "/version",
            "/ver",
            "/help"
    );

}
