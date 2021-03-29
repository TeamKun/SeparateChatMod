package net.kunmc.lab.separatechatmod;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

public class NewChatGuiExt extends NewChatGui {
    private final Minecraft mc;
    private final NewChatGuiInner systemMessageGui;
    private final NewChatGuiInner chatMessageGui;
    private int lastUpdateCounter;

    public NewChatGuiExt(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
        this.systemMessageGui = new NewChatGuiInner(mcIn) {
            @Override
            public int getChatHeight() {
                return getSystemMessageHeight();
            }
        };
        this.chatMessageGui = new NewChatGuiInner(mcIn) {
            @Override
            public int getChatHeight() {
                return getChatMessageHeight();
            }
        };
    }

    @Override
    public void func_238492_a_(MatrixStack matrixStack, int updateCounter) {
        lastUpdateCounter = updateCounter;
        chatMessageGui.func_238492_a_(matrixStack, updateCounter);
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0, 1 + mc.fontRenderer.FONT_HEIGHT * (systemMessageGui.drawChatCount(lastUpdateCounter) - 1), 0);
        systemMessageGui.func_238492_a_(matrixStack, updateCounter);
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
    }

    @Override
    public void clearChatMessages(boolean clearSentMsgHistory) {
        systemMessageGui.clearChatMessages(clearSentMsgHistory);
        chatMessageGui.clearChatMessages(clearSentMsgHistory);
    }

    @Override
    public void printChatMessage(ITextComponent textComponent) {
        if (isChatMessage(textComponent)) {
            chatMessageGui.printChatMessage(textComponent);
        } else {
            systemMessageGui.printChatMessage(textComponent);
        }
    }

    @Override
    public void refreshChat() {
        systemMessageGui.refreshChat();
        chatMessageGui.refreshChat();
    }

    @Override
    public List<String> getSentMessages() {
        return systemMessageGui.getSentMessages();
    }

    @Override
    public void addToSentMessages(String message) {
        systemMessageGui.addToSentMessages(message);
    }

    @Override
    public void resetScroll() {
        systemMessageGui.resetScroll();
        chatMessageGui.resetScroll();
    }

    @Override
    public void addScrollPos(double posInc) {
        double x = mc.mouseHelper.getMouseX() * mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth();
        double y = mc.mouseHelper.getMouseY() * mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight();
        if (isInSystemMessageRange(x, y)) {
            systemMessageGui.addScrollPos(posInc);
        } else if (isInChatMessageRange(x, y)) {
            chatMessageGui.addScrollPos(posInc);
        }
    }

    @Override
    public boolean func_238491_a_(double x, double y) {
        if (isInSystemMessageRange(x, y)) {
            int base = mc.getMainWindow().getScaledHeight() - mc.fontRenderer.FONT_HEIGHT * systemMessageGui.drawChatCount(lastUpdateCounter) - 40;
            return systemMessageGui.func_238491_a_(x, y + base);
        } else if (isInChatMessageRange(x, y)) {
            return chatMessageGui.func_238491_a_(x, y);
        } else {
            return false;
        }
    }

    @Override
    public Style func_238494_b_(double x, double y) {
        if (isInSystemMessageRange(x, y)) {
            int base = mc.getMainWindow().getScaledHeight() - mc.fontRenderer.FONT_HEIGHT * systemMessageGui.drawChatCount(lastUpdateCounter) - 40;
            return systemMessageGui.func_238494_b_(x, y + base);
        } else if (isInChatMessageRange(x, y)) {
            return chatMessageGui.func_238494_b_(x, y);
        } else {
            return null;
        }
    }

    private boolean getChatOpen() {
        return this.mc.currentScreen instanceof ChatScreen;
    }

    @Override
    public int getChatHeight() {
        return getLineCount() * 9;
    }

    @Override
    public int getLineCount() {
        double x = mc.mouseHelper.getMouseX() * mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth();
        double y = mc.mouseHelper.getMouseY() * mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight();
        if (isInSystemMessageRange(x, y)) {
            return systemMessageGui.getLineCount();
        } else {
            return chatMessageGui.getLineCount();
        }
    }

    @Override
    public void func_238495_b_(ITextComponent textComponent) {
        if (isChatMessage(textComponent)) {
            chatMessageGui.printChatMessage(textComponent);
        } else {
            systemMessageGui.printChatMessage(textComponent);
        }
    }

    private boolean isChatMessage(ITextComponent component) {
        String text = component.getString();
        List<? extends String> patterns = ModConfiguration.chatPatterns.get();
        for (String pattern : patterns) {
            if (Pattern.compile(pattern).matcher(text).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInSystemMessageRange(double x, double y) {
        double scale = getScale();
        int base = mc.fontRenderer.FONT_HEIGHT * systemMessageGui.drawChatCount(lastUpdateCounter);
        x = MathHelper.floor((x - 2) / scale);
        y = MathHelper.floor((base - y) / scale);
        int count = systemMessageGui.getLineCount();
        return 0 <= x && x <= MathHelper.floor(getChatWidth() / scale) && 0 <= y && y < 10 * count;
    }

    private boolean isInChatMessageRange(double x, double y) {
        double scale = getScale();
        x = MathHelper.floor((x - 2) / scale);
        y = MathHelper.floor((mc.getMainWindow().getScaledHeight() - y - 40) / scale);
        int count = chatMessageGui.getLineCount();
        return 0 <= x && x <= MathHelper.floor(getChatWidth() / scale) && 0 <= y && y < 10 * count;
    }

    private int getSystemMessageHeight() {
        return (getChatOpen() ? ModConfiguration.focusedSystemMessageHeight : ModConfiguration.unfocusedSystemMessageHeight).get() * 9;
    }

    private int getChatMessageHeight() {
        return (getChatOpen() ? ModConfiguration.focusedChatMessageHeight : ModConfiguration.unfocusedChatMessageHeight).get() * 9;
    }
}
