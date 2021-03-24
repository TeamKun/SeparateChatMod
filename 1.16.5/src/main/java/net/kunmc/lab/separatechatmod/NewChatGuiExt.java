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

public class NewChatGuiExt extends NewChatGui {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = new ArrayList<>();
    private final List<ChatLine<ITextComponent>> chatLines = new ArrayList<>();
    private final List<ChatLine<IReorderingProcessor>> drawnChatLines = new ArrayList<>();
    private final Deque<ITextComponent> messageQueue = new ArrayDeque<>();
    private int scrollPos;
    private boolean isScrolled;
    private long field_238490_l_ = 0L;

    public NewChatGuiExt(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
    }

    @Override
    public void func_238492_a_(MatrixStack matrixStack, int p_238492_2_) {
        if (!isChatHidden()) {
            func_238498_k_();
            int lineCount = getLineCount();
            int drawnChatLineSize = drawnChatLines.size();
            if (drawnChatLineSize > 0) {
                boolean chatOpen = getChatOpen();
                double scale = getScale();
                int width = MathHelper.ceil((double)getChatWidth() / scale);
                RenderSystem.pushMatrix();
                RenderSystem.translatef(2.0F, 8.0F, 0.0F);
                RenderSystem.scaled(scale, scale, 1.0D);
                double chatOpacity = mc.gameSettings.chatOpacity * (double)0.9F + (double)0.1F;
                double backgroundOpacity = mc.gameSettings.accessibilityTextBackgroundOpacity;
                double d3 = 9.0D * (mc.gameSettings.chatLineSpacing + 1.0D);
                double d4 = -8.0D * (mc.gameSettings.chatLineSpacing + 1.0D) + 4.0D * mc.gameSettings.chatLineSpacing;
                int l = 0;
                for (int i = 0; i + scrollPos < drawnChatLines.size() && i < lineCount; i++) {
                    ChatLine<IReorderingProcessor> chatline = drawnChatLines.get(i + scrollPos);
                    if (chatline != null) {
                        int counter = p_238492_2_ - chatline.getUpdatedCounter();
                        if (counter < 200 || chatOpen) {
                            double lineBrightness = chatOpen ? 1.0D : getLineBrightness(counter);
                            int brightness = (int)(255.0D * lineBrightness * chatOpacity);
                            int backgroundBrightness = (int)(255.0D * lineBrightness * backgroundOpacity);
                            l++;
                            if (brightness > 3) {
                                double d6 = (double)(-i) * d3;
                                matrixStack.push();
                                matrixStack.translate(0.0D, 0.0D, 50.0D);
                                fill(matrixStack, -2, (int)(d6 - d3), width + 4, (int)d6, backgroundBrightness << 24);
                                RenderSystem.enableBlend();
                                matrixStack.translate(0.0D, 0.0D, 50.0D);
                                mc.fontRenderer.drawTextWithShadow(matrixStack, chatline.getLineString(), 0.0F, (float)((int)(d6 + d4)), 16777215 + (brightness << 24));
                                RenderSystem.disableAlphaTest();
                                RenderSystem.disableBlend();
                                matrixStack.pop();
                            }
                        }
                    }
                }
                if (!messageQueue.isEmpty()) {
                    int k2 = (int)(128.0D * chatOpacity);
                    int i3 = (int)(255.0D * backgroundOpacity);
                    matrixStack.push();
                    matrixStack.translate(0.0D, 0.0D, 50.0D);
                    fill(matrixStack, -2, 0, width + 4, 9, i3 << 24);
                    RenderSystem.enableBlend();
                    matrixStack.translate(0.0D, 0.0D, 50.0D);
                    mc.fontRenderer.drawTextWithShadow(matrixStack, new TranslationTextComponent("chat.queue", messageQueue.size()), 0.0F, 1.0F, 16777215 + (k2 << 24));
                    matrixStack.pop();
                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                }
                if (chatOpen) {
                    int l2 = 9;
                    RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
                    int j3 = drawnChatLineSize * l2 + drawnChatLineSize;
                    int k3 = l * l2 + l;
                    int l3 = scrollPos * k3 / drawnChatLineSize;
                    int k1 = k3 * k3 / j3;
                    if (j3 != k3) {
                        int i4 = l3 > 0 ? 170 : 96;
                        int j4 = isScrolled ? 13382451 : 3355562;
                        fill(matrixStack, 0, -l3, 2, -l3 - k1, j4 + (i4 << 24));
                        fill(matrixStack, 2, -l3, 1, -l3 - k1, 13421772 + (i4 << 24));
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
        messageQueue.clear();
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

    private void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
        setChatLine(chatComponent, chatLineId, mc.ingameGUI.getTicks(), false);
        LOGGER.info("[CHAT] {}", chatComponent.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void setChatLine(ITextComponent p_238493_1_, int p_238493_2_, int p_238493_3_, boolean p_238493_4_) {
        if (p_238493_2_ != 0) {
            deleteChatLine(p_238493_2_);
        }
        int i = MathHelper.floor((double)getChatWidth() / getScale());
        List<IReorderingProcessor> list = RenderComponentsUtil.func_238505_a_(p_238493_1_, i, mc.fontRenderer);
        boolean flag = getChatOpen();
        for (IReorderingProcessor ireorderingprocessor : list) {
            if (flag && scrollPos > 0) {
                isScrolled = true;
                addScrollPos(1.0D);
            }
            drawnChatLines.add(0, new ChatLine<>(p_238493_3_, ireorderingprocessor, p_238493_2_));
        }
        while (drawnChatLines.size() > 100) {
            drawnChatLines.remove(drawnChatLines.size() - 1);
        }
        if (!p_238493_4_) {
            chatLines.add(0, new ChatLine<>(p_238493_3_, p_238493_1_, p_238493_2_));
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
            ChatLine<ITextComponent> chatline = chatLines.get(i);
            setChatLine(chatline.getLineString(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
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
    public void addScrollPos(double posInc) {
        scrollPos = (int)((double)scrollPos + posInc);
        int i = drawnChatLines.size();
        if (scrollPos > i - getLineCount()) {
            scrollPos = i - getLineCount();
        }
        if (scrollPos <= 0) {
            scrollPos = 0;
            isScrolled = false;
        }
    }

    @Override
    public boolean func_238491_a_(double x, double y) {
        if (getChatOpen() && !mc.gameSettings.hideGUI && !isChatHidden() && !messageQueue.isEmpty()) {
            double d0 = x - 2.0D;
            double d1 = (double)mc.getMainWindow().getScaledHeight() - y - 40.0D;
            if (d0 <= (double)MathHelper.floor((double)getChatWidth() / getScale()) && d1 < 0.0D && d1 > (double)MathHelper.floor(-9.0D * getScale())) {
                printChatMessage(messageQueue.remove());
                field_238490_l_ = System.currentTimeMillis();
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public Style func_238494_b_(double x, double y) {
        if (getChatOpen() && !mc.gameSettings.hideGUI && !isChatHidden()) {
            double d0 = x - 2.0D;
            double d1 = (double)mc.getMainWindow().getScaledHeight() - y - 40.0D;
            d0 = MathHelper.floor(d0 / getScale());
            d1 = MathHelper.floor(d1 / (getScale() * (mc.gameSettings.chatLineSpacing + 1.0D)));
            if (!(d0 < 0.0D) && !(d1 < 0.0D)) {
                int i = Math.min(getLineCount(), drawnChatLines.size());
                if (d0 <= (double)MathHelper.floor((double)getChatWidth() / getScale()) && d1 < (double)(9 * i + i)) {
                    int j = (int)(d1 / 9.0D + (double)scrollPos);
                    if (j >= 0 && j < drawnChatLines.size()) {
                        ChatLine<IReorderingProcessor> chatline = drawnChatLines.get(j);
                        return mc.fontRenderer.getCharacterManager().func_243239_a(chatline.getLineString(), (int)d0);
                    }
                }
            }
        }
        return null;
    }

    private boolean getChatOpen() {
        return mc.currentScreen instanceof ChatScreen;
    }

    private void deleteChatLine(int id) {
        drawnChatLines.removeIf(chatLine -> chatLine.getChatLineID() == id);
        chatLines.removeIf(chatLine -> chatLine.getChatLineID() == id);
    }

    @Override
    public int getChatWidth() {
        return calculateChatboxWidth(mc.gameSettings.chatWidth);
    }

    @Override
    public int getChatHeight() {
        return calculateChatboxHeight((getChatOpen() ? mc.gameSettings.chatHeightFocused : mc.gameSettings.chatHeightUnfocused) / (mc.gameSettings.chatLineSpacing + 1.0D));
    }

    @Override
    public double getScale() {
        return mc.gameSettings.chatScale;
    }

    @Override
    public int getLineCount() {
        return getChatHeight() / 9;
    }

    private long getChatDelay() {
        return (long)(mc.gameSettings.chatDelay * 1000.0D);
    }

    private void func_238498_k_() {
        if (!messageQueue.isEmpty()) {
            long millis = System.currentTimeMillis();
            if (millis - field_238490_l_ >= getChatDelay()) {
                printChatMessage(messageQueue.remove());
                field_238490_l_ = millis;
            }
        }
    }

    @Override
    public void func_238495_b_(ITextComponent textComponent) {
        if (mc.gameSettings.chatDelay <= 0.0D) {
            printChatMessage(textComponent);
        } else {
            long i = System.currentTimeMillis();
            if (i - field_238490_l_ >= getChatDelay()) {
                printChatMessage(textComponent);
                field_238490_l_ = i;
            } else {
                messageQueue.add(textComponent);
            }
        }
    }
}
