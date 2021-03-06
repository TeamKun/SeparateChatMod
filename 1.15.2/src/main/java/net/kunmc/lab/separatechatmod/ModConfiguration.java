package net.kunmc.lab.separatechatmod;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ModConfiguration {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Integer> focusedChatMessageHeight;
    public static final ForgeConfigSpec.ConfigValue<Integer> focusedSystemMessageHeight;
    public static final ForgeConfigSpec.ConfigValue<Integer> unfocusedChatMessageHeight;
    public static final ForgeConfigSpec.ConfigValue<Integer> unfocusedSystemMessageHeight;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> chatPatterns;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        focusedChatMessageHeight = builder.define("focusedChatMessageHeight", 17);
        focusedSystemMessageHeight = builder.define("focusedSystemMessageHeight", 3);
        unfocusedChatMessageHeight = builder.define("unfocusedChatMessageHeight", 7);
        unfocusedSystemMessageHeight = builder.define("unfocusedSystemMessageHeight", 3);
        chatPatterns = builder.defineList("chatPatterns", Arrays.asList("<\\w{3,16}> .+", "\\[\\w{3,16}] .+", "\\w{3,16} whispers to you: .+", "You whisper to \\w{3,16}: .+", "\\w{3,16} \u306b\u3055\u3055\u3084\u304b\u308c\u307e\u3057\u305f\uff1a.+", "\\w{3,16} \u306b\u3055\u3055\u3084\u304d\u307e\u3057\u305f\uff1a.+"), obj -> obj instanceof String);
        SPEC = builder.build();
    }
}
