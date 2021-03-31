package net.kunmc.lab.separatechatmod;

@net.minecraftforge.common.config.Config(modid = SeparateChatMod.MOD_ID, name = SeparateChatMod.MOD_ID)
public class ModConfiguration {
    public static int focusedChatMessageHeight = 17;
    public static int focusedSystemMessageHeight = 3;
    public static int unfocusedChatMessageHeight = 7;
    public static int unfocusedSystemMessageHeight = 3;
    public static String[] chatPatterns = new String[] {"<\\w{3,16}> .+", "\\[Server] .+", "\\w{3,16} whispers to you: .+", "You whisper to \\w{3,16}: .+", "\\w{3,16} にささやかれました: .+", "\\w{3,16} にささやきました: .+"};
}
