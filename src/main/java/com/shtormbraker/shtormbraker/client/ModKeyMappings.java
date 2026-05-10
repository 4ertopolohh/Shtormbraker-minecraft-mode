package com.shtormbraker.shtormbraker.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class ModKeyMappings {
    public static final String CATEGORY = "key.categories.shtormbraker";

    public static final KeyMapping STRIKE_LIGHTNING = new KeyMapping(
            "key.shtormbraker.strike_lightning",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            CATEGORY
    );

    public static final KeyMapping START_STORM = new KeyMapping(
            "key.shtormbraker.start_storm",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    private ModKeyMappings() {
    }
}
