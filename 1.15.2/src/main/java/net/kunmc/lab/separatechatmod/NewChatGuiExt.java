package net.kunmc.lab.separatechatmod;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class NewChatGuiExt extends NewChatGui {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.newArrayList();
    private final List<ChatLine> chatLines = Lists.newArrayList();
    private final List<ChatLine> drawnChatLines = Lists.newArrayList();
    private int scrollPos;
    private boolean isScrolled;

    public NewChatGuiExt(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
    }

    @Override
    public void render(int updateCounter) {
        if (!isChatHidden()) {
            int lineCount = getLineCount();
            int drawnChatLineSize = drawnChatLines.size();
            if (drawnChatLineSize > 0) {
                boolean chatOpen = getChatOpen();
                double scale = getScale();
                int width = MathHelper.ceil((double)getChatWidth() / scale);
                RenderSystem.pushMatrix();
                RenderSystem.translatef(2.0F, 8.0F, 0.0F);
                RenderSystem.scaled(scale, scale, 1.0D);
                double chatOpacity = mc.gameSettings.chatOpacity * 0.9D + 0.1D;
                double backgroundOpacity = mc.gameSettings.accessibilityTextBackgroundOpacity;
                int l = 0;
                Matrix4f matrix4f = Matrix4f.func_226599_b_(0.0F, 0.0F, -100.0F);
                for (int i = 0; i + scrollPos < drawnChatLines.size() && i < lineCount; i++) {
                    ChatLine chatline = drawnChatLines.get(i + scrollPos);
                    if (chatline != null) {
                        int counter = updateCounter - chatline.getUpdatedCounter();
                        if (counter < 200 || chatOpen) {
                            double lineBrightness = chatOpen ? 1.0D : getLineBrightness(counter);
                            int brightness = (int)(255.0D * lineBrightness * chatOpacity);
                            int backgroundBrightness = (int)(255.0D * lineBrightness * backgroundOpacity);
                            l++;
                            if (brightness > 3) {
                                int k2 = -i * 9;
                                fill(matrix4f, -2, k2 - 9, width + 4, k2, backgroundBrightness << 24);
                                String s = chatline.getChatComponent().getFormattedText();
                                RenderSystem.enableBlend();
                                mc.fontRenderer.drawStringWithShadow(s, 0.0F, k2 - 8.0F, 16777215 + (brightness << 24));
                                RenderSystem.disableAlphaTest();
                                RenderSystem.disableBlend();
                            }
                        }
                    }
                }
                if (chatOpen) {
                    int l2 = 9;
                    RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
                    int i3 = drawnChatLineSize * l2 + drawnChatLineSize;
                    int j3 = l * l2 + l;
                    int k3 = scrollPos * j3 / drawnChatLineSize;
                    int k1 = j3 * j3 / i3;
                    if (i3 != j3) {
                        int l3 = k3 > 0 ? 170 : 96;
                        int i4 = isScrolled ? 13382451 : 3355562;
                        fill(0, -k3, 2, -k3 - k1, i4 + (l3 << 24));
                        fill(2, -k3, 1, -k3 - k1, 13421772 + (l3 << 24));
                    }
                }
                RenderSystem.popMatrix();
            }
        }
    }

    private boolean isChatHidden() {
        return mc.gameSettings.chatVisibility == ChatVisibility.HIDDEN;
    }

    private static double getLineBrightness(int counterIn) {
        double d0 = MathHelper.clamp((1.0D - (double)counterIn / 200.0D) * 10.0D, 0.0D, 1.0D);
        return d0 * d0;
    }

    @Override
    public void clearChatMessages(boolean clearSentMsgHistory) {
        drawnChatLines.clear();
        chatLines.clear();
        if (clearSentMsgHistory) {
            sentMessages.clear();
        }
    }

    @Override
    public void printChatMessage(ITextComponent chatComponent) {
        printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    @Override
    public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
        setChatLine(chatComponent, chatLineId, mc.ingameGUI.getTicks(), false);
        LOGGER.info("[CHAT] {}", chatComponent.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        if (chatLineId != 0) {
            deleteChatLine(chatLineId);
        }
        int i = MathHelper.floor((double)getChatWidth() / getScale());
        List<ITextComponent> list = RenderComponentsUtil.splitText(chatComponent, i, mc.fontRenderer, false, false);
        boolean flag = getChatOpen();
        for (ITextComponent itextcomponent : list) {
            if (flag && scrollPos > 0) {
                isScrolled = true;
                func_194813_a(1.0D);
            }
            drawnChatLines.add(0, new ChatLine(updateCounter, itextcomponent, chatLineId));
        }
        while (drawnChatLines.size() > 100) {
            drawnChatLines.remove(drawnChatLines.size() - 1);
        }
        if (!displayOnly) {
            chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));
            while (chatLines.size() > 100) {
                chatLines.remove(chatLines.size() - 1);
            }
        }
    }

    @Override
    public void refreshChat() {
        drawnChatLines.clear();
        resetScroll();
        for (int i = chatLines.size() - 1; i >= 0; --i) {
            ChatLine chatline = chatLines.get(i);
            setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    @Override
    public List<String> getSentMessages() {
        return sentMessages;
    }

    @Override
    public void addToSentMessages(String message) {
        if (sentMessages.isEmpty() || !sentMessages.get(sentMessages.size() - 1).equals(message)) {
            sentMessages.add(message);
        }
    }

    @Override
    public void resetScroll() {
        scrollPos = 0;
        isScrolled = false;
    }

    @Override
    public void func_194813_a(double p_194813_1_) {
        scrollPos = (int)((double)scrollPos + p_194813_1_);
        int i = drawnChatLines.size();
        if (scrollPos > i - getLineCount()) {
            scrollPos = i - getLineCount();
        }
        if (scrollPos <= 0) {
            scrollPos = 0;
            isScrolled = false;
        }
    }

    @Nullable
    @Override
    public ITextComponent getTextComponent(double p_194817_1_, double p_194817_3_) {
        if (getChatOpen() && !mc.gameSettings.hideGUI && !isChatHidden()) {
            double d0 = getScale();
            double d1 = p_194817_1_ - 2.0D;
            double d2 = (double)mc.func_228018_at_().getScaledHeight() - p_194817_3_ - 40.0D;
            d1 = MathHelper.floor(d1 / d0);
            d2 = MathHelper.floor(d2 / d0);
            if (!(d1 < 0.0D) && !(d2 < 0.0D)) {
                int i = Math.min(getLineCount(), drawnChatLines.size());
                if (d1 <= (double)MathHelper.floor((double)getChatWidth() / getScale()) && d2 < (double)(9 * i + i)) {
                    int j = (int)(d2 / 9.0D + (double)scrollPos);
                    if (j >= 0 && j < drawnChatLines.size()) {
                        ChatLine chatline = drawnChatLines.get(j);
                        int k = 0;
                        for (ITextComponent itextcomponent : chatline.getChatComponent()) {
                            if (itextcomponent instanceof StringTextComponent) {
                                k += mc.fontRenderer.getStringWidth(RenderComponentsUtil.removeTextColorsIfConfigured(((StringTextComponent)itextcomponent).getText(), false));
                                if ((double)k > d1) {
                                    return itextcomponent;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean getChatOpen() {
        return mc.currentScreen instanceof ChatScreen;
    }

    @Override
    public void deleteChatLine(int id) {
        Iterator<ChatLine> iterator = drawnChatLines.iterator();
        while (iterator.hasNext()) {
            ChatLine chatline = iterator.next();
            if (chatline.getChatLineID() == id) {
                iterator.remove();
            }
        }
        iterator = chatLines.iterator();
        while (iterator.hasNext()) {
            ChatLine chatline1 = iterator.next();
            if (chatline1.getChatLineID() == id) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public int getChatWidth() {
        return calculateChatboxWidth(mc.gameSettings.chatWidth);
    }

    @Override
    public int getChatHeight() {
        return calculateChatboxHeight(getChatOpen() ? mc.gameSettings.chatHeightFocused : mc.gameSettings.chatHeightUnfocused);
    }

    @Override
    public double getScale() {
        return mc.gameSettings.chatScale;
    }

    @Override
    public int getLineCount() {
        return getChatHeight() / 9;
    }
}
