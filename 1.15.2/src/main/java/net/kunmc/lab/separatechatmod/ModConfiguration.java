package net.kunmc.lab.separatechatmod;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfiguration {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Integer> focusedChatMessageHeight;
    public static final ForgeConfigSpec.ConfigValue<Integer> focusedSystemMessageHeight;
    public static final ForgeConfigSpec.ConfigValue<Integer> unfocusedChatMessageHeight;
    public static final ForgeConfigSpec.ConfigValue<Integer> unfocusedSystemMessageHeight;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        focusedChatMessageHeight = builder.define("focusedChatMessageHeight", 7);
        focusedSystemMessageHeight = builder.define("focusedSystemMessageHeight", 3);
        unfocusedChatMessageHeight = builder.define("unfocusedChatMessageHeight", 7);
        unfocusedSystemMessageHeight = builder.define("unfocusedSystemMessageHeight", 3);
        SPEC = builder.build();
    }
}
