package com.cb2495.modpackhelper;

import com.cb2495.modpackhelper.help.HelpReadState;
import com.cb2495.modpackhelper.screen.HelperScreen;
import com.cb2495.modpackhelper.screen.QandAScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Modpack Helper：整合包的帮助与引导模组。
 * <p>负责帮助文档、常见问题、旧存档导入等与 Verity 本体无关的「周边功能」。
 * 这些功能原来长在 VerityPlus（modid {@code verityplus}）里，
 * 现在拆出来独立发布，由本模组依赖 VerityPlus 取得所需的共用工具。
 * <p>模组本身没有方块/物品，只向客户端注册生命周期回调与两条指令。
 */
@Mod(ModPackHelper.MODID)
public class ModPackHelper {

    /** 必须与 {@code META-INF/mods.toml} 里的 modId 一致；资源路径也以它为命名空间。 */
    public static final String MODID = "modpackhelper";

    /** 依赖的 VerityPlus 的 modid；本模组直接调用它的类，改了这里就会起不来。 */
    public static final String VERITYPLUS_MODID = "verityplus";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ModPackHelper() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        // 帮助页的「已读」记录会被界面同步读取；这里只是提前把文件读一次，
        // 让第一次打开帮助页时不必在渲染线程上读盘
        event.enqueueWork(HelpReadState::preload);
    }

    /**
     * 客户端指令：{@code /mph help [主题]} 与 {@code /mph qanda}。
     * <p>用单独的 {@code mph} 根指令而不是并进 {@code /vc}：本模组与 VerityPlus
     * 是两个 jar，指令也各归各的，卸掉一个不会带走另一个的入口。
     */
    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("mph")
                        .executes(ctx -> {
                            open(new QandAScreen());
                            return 1;
                        })
                        .then(Commands.literal("help")
                                .executes(ctx -> {
                                    open(new HelperScreen("low_memory"));
                                    return 1;
                                })
                                .then(Commands.argument("topic", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> {
                                            for (String title : HelperScreen.topicTitles()) {
                                                builder.suggest(title);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            // 允许直接输入中文名，内部统一转成英文 topic
                                            String input = StringArgumentType.getString(ctx, "topic");
                                            String topic = HelperScreen.resolveTopic(input);
                                            // 主题不存在时直接在主线程开路重，而不是让玩家看到一个
                                            // 「无法加载帮助内容」的空页面还不知道哪里写错了
                                            if (!HelperScreen.topicExists(topic)) {
                                                sendError("未找到帮助主题: " + input);
                                                return 0;
                                            }
                                            open(new HelperScreen(topic));
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("qanda")
                                .executes(ctx -> {
                                    open(new QandAScreen());
                                    return 1;
                                })
                        )
        );
    }

    /** 切换到某个界面；没有聊天栏（例如从主界面进来的指令）时也能用。 */
    private static void open(net.minecraft.client.gui.screens.Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }

    /**
     * 在聊天栏输出一条红色错误。
     * <p>指令执行时玩家一定在游戏里，不需要再判空；用 {@code tell} 切到主线程，
     * 避免将来从非主线程调用时碰到组件渲染的线程问题。
     */
    private static void sendError(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            LOGGER.warn(message);
            return;
        }
        mc.player.displayClientMessage(
                Component.literal("[Modpack Helper] ").withStyle(ChatFormatting.RED)
                        .append(Component.literal(message).withStyle(ChatFormatting.WHITE)),
                false);
    }

    /** 当前玩家名；尚未登录时为 {@code ?}。 */
    static String clientName() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getUser() == null ? "?" : minecraft.getUser().getName();
    }

    /**
     * 客户端侧的生命周期钩子。
     * <p>只打印一行日志，方便确认「Modpack Helper 已随 VerityPlus 一起加载」。
     */
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        private ClientEvents() {}

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Modpack Helper 客户端初始化完成，当前玩家: {}", clientName());
        }
    }
}
