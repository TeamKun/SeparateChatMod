package net.kunmc.lab.separatechatmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

public class GuiNewChatExt extends GuiNewChat {
    private final Minecraft mc;
    private final GuiNewChat systemMessageGui;
    private final GuiNewChat chatMessageGui;

    public GuiNewChatExt(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
        this.systemMessageGui = new GuiNewChat(mcIn) {
            @Override
            public int getChatHeight() {
                return getSystemMessageHeight();
            }
        };
        this.chatMessageGui = new GuiNewChat(mcIn) {
            @Override
            public int getChatHeight() {
                return getChatMessageHeight();
            }
        };
    }

    @Override
    public void drawChat(int updateCounter) {
        chatMessageGui.drawChat(updateCounter);
        GlStateManager.translate(0, -getChatMessageHeight() - getInterval(), 0);
        systemMessageGui.drawChat(updateCounter);
    }

    @Override
    public void clearChatMessages(boolean clearSentMsgHistory) {
        systemMessageGui.clearChatMessages(clearSentMsgHistory);
        chatMessageGui.clearChatMessages(clearSentMsgHistory);
    }

    @Override
    public void printChatMessage(ITextComponent chatComponent) {
        printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    @Override
    public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
        if (isChatMessage(chatComponent)) {
            chatMessageGui.printChatMessageWithOptionalDeletion(chatComponent, chatLineId);
        } else {
            systemMessageGui.printChatMessageWithOptionalDeletion(chatComponent, chatLineId);
        }
    }

    @Override
    public void refreshChat() {
        chatMessageGui.refreshChat();
        systemMessageGui.refreshChat();
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
        chatMessageGui.resetScroll();
        systemMessageGui.resetScroll();
    }

    @Override
    public void scroll(int amount) {
        ScaledResolution resolution = new ScaledResolution(mc);
        int factor = resolution.getScaleFactor();
        int x = Mouse.getX() / factor;
        int y = Mouse.getY() / factor;
        if (isInSystemMessageRange(x, y)) {
            systemMessageGui.scroll(amount);
        } else if (isInChatMessageRange(x, y)) {
            chatMessageGui.scroll(amount);
        }
    }

    @Nullable
    @Override
    public ITextComponent getChatComponent(int mouseX, int mouseY) {
        ScaledResolution resolution = new ScaledResolution(mc);
        int factor = resolution.getScaleFactor();
        int x = mouseX / factor;
        int y = mouseY / factor;
        if (isInSystemMessageRange(x, y)) {
            int base = getChatMessageHeight() + getInterval();
            return systemMessageGui.getChatComponent(mouseX, mouseY - base * factor);
        } else if (isInChatMessageRange(x, y)) {
            return chatMessageGui.getChatComponent(mouseX, mouseY);
        } else {
            return null;
        }
    }

    @Override
    public boolean getChatOpen() {
        return systemMessageGui.getChatOpen();
    }

    @Override
    public void deleteChatLine(int id) {
        systemMessageGui.deleteChatLine(id);
        chatMessageGui.deleteChatLine(id);
    }

    @Override
    public int getChatHeight() {
        return getLineCount() * 9;
    }

    @Override
    public int getLineCount() {
        ScaledResolution resolution = new ScaledResolution(mc);
        int factor = resolution.getScaleFactor();
        int x = Mouse.getX() / factor;
        int y = Mouse.getY() / factor;
        if (isInSystemMessageRange(x, y)) {
            return systemMessageGui.getLineCount();
        } else {
            return chatMessageGui.getLineCount();
        }
    }

    private boolean isChatMessage(ITextComponent component) {
        String text = component.getUnformattedText();
        String[] patterns = ModConfiguration.chatPatterns;
        for (String pattern : patterns) {
            if (Pattern.compile(pattern).matcher(text).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInSystemMessageRange(int x, int y) {
        double scale = getChatScale();
        int base = getChatMessageHeight() + getInterval();
        x = MathHelper.floor((x - 2) / scale);
        y = MathHelper.floor((y - 40 - base) / scale);
        int count = chatMessageGui.getLineCount();
        return 0 <= x && x <= MathHelper.floor(getChatWidth() / scale) && 0 <= y && y < (mc.fontRenderer.FONT_HEIGHT + 1) * count;
    }

    private boolean isInChatMessageRange(int x, int y) {
        double scale = getChatScale();
        x = MathHelper.floor((x - 2) / scale);
        y = MathHelper.floor((y - 40) / scale);
        int count = chatMessageGui.getLineCount();
        return 0 <= x && x <= MathHelper.floor(getChatWidth() / scale) && 0 <= y && y < (mc.fontRenderer.FONT_HEIGHT + 1) * count;
    }

    private int getSystemMessageHeight() {
        return (getChatOpen() ? ModConfiguration.focusedSystemMessageHeight : ModConfiguration.unfocusedSystemMessageHeight) * mc.fontRenderer.FONT_HEIGHT;
    }

    private int getChatMessageHeight() {
        return (getChatOpen() ? ModConfiguration.focusedChatMessageHeight : ModConfiguration.unfocusedChatMessageHeight) * mc.fontRenderer.FONT_HEIGHT;
    }

    private int getInterval() {
        return ModConfiguration.interval;
    }
}
