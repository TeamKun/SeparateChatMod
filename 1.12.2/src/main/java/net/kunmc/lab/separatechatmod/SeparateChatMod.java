package net.kunmc.lab.separatechatmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod(modid = SeparateChatMod.MOD_ID, name = SeparateChatMod.MOD_NAME, version = SeparateChatMod.VERSION, clientSideOnly = true)
public class SeparateChatMod {
    public static final String MOD_ID = "separate-chat-mod";
    public static final String MOD_NAME = "Separate Chat Mod";
    public static final String VERSION = "1.0";

    public SeparateChatMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!(minecraft.ingameGUI.getChatGUI() instanceof GuiNewChatExt)) {
            try {
                GuiIngame guiIngame = minecraft.ingameGUI;
                Field field = ObfuscationReflectionHelper.findField(GuiIngame.class, "field_73840_e");
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.set(guiIngame, new GuiNewChatExt(minecraft));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @net.minecraftforge.common.config.Config(modid = MOD_ID, name = MOD_ID)
    public static class Config {
        public static int focusedChatMessageHeight = 17;
        public static int focusedSystemMessageHeight = 3;
        public static int unfocusedChatMessageHeight = 7;
        public static int unfocusedSystemMessageHeight = 3;
    }
}
