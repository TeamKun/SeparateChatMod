package net.kunmc.lab.separatechatmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuiNewChatExt extends GuiNewChat {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = new ArrayList<>();
    private final List<ChatLine> chatLines = new ArrayList<>();
    private final List<ChatLine> drawnChatLines = new ArrayList<>();
    private int scrollPos;
    private boolean isScrolled;

    public GuiNewChatExt(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
    }

    @Override
    public void drawChat(int updateCounter) {
        if (mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int lineCount = getLineCount();
            int drawnChatLineSize = drawnChatLines.size();
            float opacity = mc.gameSettings.chatOpacity * 0.9F + 0.1F;
            if (drawnChatLineSize > 0) {
                boolean chatOpen = getChatOpen();
                float chatScale = getChatScale();
                int width = MathHelper.ceil((float)getChatWidth() / chatScale);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 8.0F, 0.0F);
                GlStateManager.scale(chatScale, chatScale, 1.0F);
                int l = 0;
                for (int i = 0; i + scrollPos < drawnChatLines.size() && i < lineCount; i++) {
                    ChatLine chatline = drawnChatLines.get(i + scrollPos);
                    if (chatline != null) {
                        int counter = updateCounter - chatline.getUpdatedCounter();
                        if (counter < 200 || chatOpen) {
                            double lineBrightness = chatOpen ? 1.0D : getLineBrightness(counter);
                            int brightness = (int)(255.0D * lineBrightness * opacity);
                            l++;
                            if (brightness > 3) {
                                int j2 = -i * 9;
                                drawRect(-2, j2 - 9, width + 4, j2, brightness / 2 << 24);
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                mc.fontRenderer.drawStringWithShadow(s, 0.0F, (float)(j2 - 8), 16777215 + (brightness << 24));
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }
                if (chatOpen) {
                    int k2 = mc.fontRenderer.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = drawnChatLineSize * k2 + drawnChatLineSize;
                    int i3 = l * k2 + l;
                    int j3 = scrollPos * i3 / drawnChatLineSize;
                    int k1 = i3 * i3 / l2;
                    if (l2 != i3) {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = isScrolled ? 13382451 : 3355562;
                        drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }
                GlStateManager.popMatrix();
            }
        }
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
        setChatLine(chatComponent, chatLineId, mc.ingameGUI.getUpdateCounter(), false);
        LOGGER.info("[CHAT {}", chatComponent.getUnformattedText().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        if (chatLineId != 0) {
            deleteChatLine(chatLineId);
        }
        int i = MathHelper.floor((float)getChatWidth() / getChatScale());
        List<ITextComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, mc.fontRenderer, false, false);
        boolean flag = getChatOpen();
        for (ITextComponent itextcomponent : list) {
            if (flag && scrollPos > 0) {
                isScrolled = true;
                scroll(1);
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
    public void scroll(int amount) {
        scrollPos += amount;
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
    public ITextComponent getChatComponent(int mouseX, int mouseY) {
        if (!getChatOpen()) {
            return null;
        } else {
            ScaledResolution scaledresolution = new ScaledResolution(mc);
            int i = scaledresolution.getScaleFactor();
            float f = getChatScale();
            int j = mouseX / i - 2;
            int k = mouseY / i - 40;
            j = MathHelper.floor((float)j / f);
            k = MathHelper.floor((float)k / f);
            if (j >= 0 && k >= 0) {
                int l = Math.min(getLineCount(), drawnChatLines.size());
                if (j <= MathHelper.floor((float)getChatWidth() / getChatScale()) && k < mc.fontRenderer.FONT_HEIGHT * l + l) {
                    int i1 = k / mc.fontRenderer.FONT_HEIGHT + scrollPos;
                    if (i1 >= 0 && i1 < drawnChatLines.size()) {
                        ChatLine chatline = drawnChatLines.get(i1);
                        int j1 = 0;
                        for (ITextComponent itextcomponent : chatline.getChatComponent()) {
                            if (itextcomponent instanceof TextComponentString) {
                                j1 += mc.fontRenderer.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(((TextComponentString)itextcomponent).getText(), false));
                                if (j1 > j) {
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
        return mc.currentScreen instanceof GuiChat;
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
    public float getChatScale() {
        return mc.gameSettings.chatScale;
    }

    @Override
    public int getLineCount() {
        return getChatHeight() / 9;
    }
}
