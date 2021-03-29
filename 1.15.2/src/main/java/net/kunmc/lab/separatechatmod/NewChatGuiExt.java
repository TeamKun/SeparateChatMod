package net.kunmc.lab.separatechatmod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
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
    public void render(int updateCounter) {
        lastUpdateCounter = updateCounter;
        chatMessageGui.render(updateCounter);
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0, 1 + mc.fontRenderer.FONT_HEIGHT * (systemMessageGui.drawChatCount(lastUpdateCounter) - 1), 0);
        systemMessageGui.render(updateCounter);
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
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
    public void func_194813_a(double p_194813_1_) {
        double x = mc.mouseHelper.getMouseX() * mc.func_228018_at_().getScaledWidth() / mc.func_228018_at_().getWidth();
        double y = mc.mouseHelper.getMouseY() * mc.func_228018_at_().getScaledHeight() / mc.func_228018_at_().getHeight();
        if (isInSystemMessageRange(x, y)) {
            systemMessageGui.func_194813_a(p_194813_1_);
        } else if (isInChatMessageRange(x, y)) {
            chatMessageGui.func_194813_a(p_194813_1_);
        }
    }

    @Nullable
    @Override
    public ITextComponent getTextComponent(double x, double y) {
        if (isInSystemMessageRange(x, y)) {
            int base = mc.func_228018_at_().getScaledHeight() - mc.fontRenderer.FONT_HEIGHT * systemMessageGui.drawChatCount(lastUpdateCounter) - 40;
            return systemMessageGui.getTextComponent(x, y + base);
        } else if (isInChatMessageRange(x, y)) {
            return chatMessageGui.getTextComponent(x, y);
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
        double x = mc.mouseHelper.getMouseX() * mc.func_228018_at_().getScaledWidth() / mc.func_228018_at_().getWidth();
        double y = mc.mouseHelper.getMouseY() * mc.func_228018_at_().getScaledHeight() / mc.func_228018_at_().getHeight();
        if (isInSystemMessageRange(x, y)) {
            return systemMessageGui.getLineCount();
        } else {
            return chatMessageGui.getLineCount();
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
        int count = chatMessageGui.getLineCount();
        return 0 <= x && x <= MathHelper.floor(getChatWidth() / scale) && 0 <= y && y < 10 * count;
    }

    private boolean isInChatMessageRange(double x, double y) {
        double scale = getScale();
        x = MathHelper.floor((x - 2) / scale);
        y = MathHelper.floor((mc.func_228018_at_().getScaledHeight() - y - 40) / scale);
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
