package net.kunmc.lab.separatechatmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod("separate-chat-mod")
@OnlyIn(Dist.CLIENT)
public class SeparateChatMod {
    public SeparateChatMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!(Minecraft.getInstance().ingameGUI.getChatGUI() instanceof NewChatGuiExt)) {
            try {
                Minecraft minecraft = Minecraft.getInstance();
                IngameGui ingameGUI = minecraft.ingameGUI;
                Field field = ObfuscationReflectionHelper.findField(IngameGui.class, "field_73840_e");
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.set(ingameGUI, new NewChatGuiExt(minecraft));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
