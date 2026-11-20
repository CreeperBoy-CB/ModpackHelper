package com.cb2495.modpackhelper.client;

import com.cb2495.modpackhelper.ModPackHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 聊天栏里「引导类消息」的处理：Verity 的教程推销、错误链接。
 * <p>这类消息与实际功能无关，纯属帮助/引导，所以跟着帮助功能一起放在本模组。
 * <p><b>不</b>在这里处理 Verity 对话的翻译：那属于 VerityPlus 的职责，
 * 它自己的过滤器优先级更高，会先把需要翻译的消息处理掉。两边同名事件的处理顺序
 * 只影响「谁先看到消息」，不影响结果——这里只碰引导类文本。
 *
 * <h3>优先级</h3>
 * {@link EventPriority#HIGHEST}：与 VerityPlus 的错误处理共用最高优先级，
 * 保证在下游的 {@code ErrorChatHandler}（NORMAL）读到消息之前就处理干净。
 */
@Mod.EventBusSubscriber(modid = ModPackHelper.MODID, value = Dist.CLIENT)
public class ChatGuideFilter {

    /** Need help setting up this mod? Watch this tutorial. 这句的识别关键词。 */
    private static final String[] SETUP_PROMPT_KEYWORDS = {
            "Need help setting up this mod?", "Watch this tutorial."
    };

    /**
     * AI 出错时那条 "AI Setup issue? Tutorial: &lt;链接&gt;" 的前缀。
     * <p>报错本身由 VerityPlus 的错误处理负责展示，这条链接只会刷屏。
     */
    private static final String AI_SETUP_PREFIX = "AI Setup issue? Tutorial:";

    /** DeepSeek 官方计价文档，「详见此处」点击后打开。 */
    private static final String PRICING_DOC_URL = "https://api-docs.deepseek.com/zh-cn/quick_start/pricing/";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClientChatReceived(ClientChatReceivedEvent event) {
        Component message = event.getMessage();
        if (message == null) return;

        String text = message.getString();

        // "Need help setting up this mod? Watch this tutorial." → 换成我们自己的引导
        if (containsAll(text, SETUP_PROMPT_KEYWORDS)) {
            event.setCanceled(true);
            sendCustomHelpMessage();
            return;
        }

        // 紧跟着的 "Setup Tutorial" 是可点击链接，第一句已经被拦下，这条没有意义
        if (text.contains("Setup Tutorial")) {
            event.setCanceled(true);
            return;
        }

        // 错误提示里的 "AI Setup issue? Tutorial: <链接>" 一并隐藏
        if (shouldHide(text)) {
            event.setCanceled(true);
        }
    }

    /**
     * 这条消息是否属于「应该直接隐藏」的引导类文本。
     * <p>公开出来是因为 VerityPlus 的 ChatFilter 也要在自己那条路径上做同样的判断，
     * 通过 {@code ModpackHelperBridge} 反射调用。
     */
    public static boolean shouldHide(String text) {
        return text.startsWith(AI_SETUP_PREFIX) || text.contains(AI_SETUP_PREFIX);
    }

    /** 关键词是否全部出现。 */
    private static boolean containsAll(String text, String... keywords) {
        for (String k : keywords) {
            if (!text.contains(k)) return false;
        }
        return true;
    }

    /**
     * 首次看到 Verity 的教程推销时，换成我们自己的引导：
     * 告诉玩家遇到问题该输什么指令，顺便说明青色文字可以点。
     * <p>峰谷提示的「怎么关」也只在这里说明一次，之后不再重复。
     */
    private static void sendCustomHelpMessage() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Component msg = Component.literal("[Modpack Helper] ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("如果你在游玩过程中遇到任何问题，随时输入 ").withStyle(ChatFormatting.WHITE))
                .append(ChatLinks.suggestCmd("/vc qanda", ChatLinks.CLICK_TO_FILL))
                .append(Component.literal(" 获取帮助，如果没有在列表里找到你的问题，也可以在 QQ 交流群中提问。").withStyle(ChatFormatting.WHITE));
        mc.player.displayClientMessage(msg, false);

        Component tip = Component.literal("温馨提示：该模组发送的消息中所有青色(淡蓝色?)字体都是可以点击并自动填入的指令，点击后可按回车直接执行")
                .withStyle(ChatFormatting.GRAY);
        mc.player.displayClientMessage(tip, false);

        sendPricingHintIntro();
    }

    /**
     * 首次下发「可关闭峰谷提示」的说明。
     * <p>峰谷提示本身每次进入存档都会发，但「怎么关」只在首次启动时说明一次；
     * 说明过就写进 VerityPlus 的配置，之后不再出现。开关已关闭或当前不是 DeepSeek
     * 时不说明，这两种情况下关掉说明会让玩家以后重新打开时再也看不到。
     * <p>不在这里发峰谷提示的正文：正文需要当天的节假日数据，
     * 同步取回会卡住启动流程，交给 {@link DeepSeekHintHandler} 在进入存档后异步发出。
     */
    private static void sendPricingHintIntro() {
        if (!VerityPlusConfigBridge.isHintEnabled()) return;
        if (!VerityPlusConfigBridge.isPricingHintIntroduced()) return;
        if (!"DeepSeek".equals(VerityPlusConfigBridge.currentProvider())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.player.displayClientMessage(
                Component.literal("[提示]").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal("DeepSeek 采用峰谷计价方式，计价规则").withStyle(ChatFormatting.WHITE))
                        .append(ChatLinks.openUrl("[详见此处]", PRICING_DOC_URL, "点击打开 DeepSeek 官方计价文档"))
                        .append(Component.literal("。若不想看到此类提示，可输入 ").withStyle(ChatFormatting.WHITE))
                        .append(ChatLinks.suggestCmd("/vc dshint off", ChatLinks.CLICK_TO_FILL))
                        .append(Component.literal(" 关闭。").withStyle(ChatFormatting.WHITE)),
                false);

        VerityPlusConfigBridge.markPricingHintIntroduced();
    }
}
