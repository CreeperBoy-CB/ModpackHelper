package com.cb2495.modpackhelper.screen;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

/**
 * 「模组管理功能已移除」的说明界面。
 * <p>这个界面原先长在 VerityPlus 里，随模组管理功能一起下线，现在搬到这里：
 * 它要链接的「模组安装」「模组管理」两份教程都属于本模组，放在一起才不会再出现
 * 「配置界面链接到一个没装的模组」的断裂。
 * <p>三个按钮与欢迎页、导入页保持一致的底部布局；「跳过」直接完成流程。
 * <p>若没装 Modpack Helper（本模组），VerityPlus 会退回到自己的一条简易说明。
 */
public class ModsRemovedScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** 正文行距与起始 Y。标题栏下方留一段空白再开始正文。 */
    private static final int TEXT_TOP = 64;
    private static final int LINE_GAP = 16;
    private static final int TEXT_COLOR = 0xFFDDDDDD;

    /** 底部按钮行距离屏幕底部的边距，与其它引导页一致。 */
    private static final int BUTTON_BOTTOM_MARGIN = 50;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 100;
    /** 相邻按钮之间的间隙。 */
    private static final int BUTTON_GAP = 5;

    /** 本页结束后的回调：首次启动流程里去收尾，其它入口直接退出界面。 */
    private final Runnable onFinished;

    /**
     * @param onFinished 点「跳过」（或按 Esc）之后要做的事
     */
    public ModsRemovedScreen(Runnable onFinished) {
        super(Component.literal("模组管理"));
        this.onFinished = onFinished;
    }

    @Override
    protected void init() {
        super.init();

        int buttonY = this.height - BUTTON_BOTTOM_MARGIN;
        // 三个按钮等宽，整行按「按钮 + 间隙」的实际总宽居中
        int totalWidth = BUTTON_WIDTH * 3 + BUTTON_GAP * 2;
        int startX = (this.width - totalWidth) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("模组安装"), btn -> {
            Minecraft.getInstance().setScreen(new HelperScreen("mod_install", this));
        }).pos(startX, buttonY).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.literal("模组管理"), btn -> {
            Minecraft.getInstance().setScreen(new HelperScreen("mod_manage", this));
        }).pos(startX + BUTTON_WIDTH + BUTTON_GAP, buttonY).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.literal("跳过"), btn -> {
            finish();
        }).pos(startX + (BUTTON_WIDTH + BUTTON_GAP) * 2, buttonY).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    /** 本页结束：有回调就交给调用方，否则直接退出界面。 */
    private void finish() {
        try {
            if (onFinished != null) {
                onFinished.run();
            } else {
                Minecraft.getInstance().setScreen(null);
            }
        } catch (RuntimeException e) {
            // 回调属于调用方（首次启动收尾），它自己的异常不该把玩家卡在说明页里
            LOGGER.error("模组管理说明页收尾失败", e);
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        int y = TEXT_TOP;
        graphics.drawCenteredString(this.font,
                Component.literal("模组管理功能已经移除"),
                this.width / 2, y, 0xFFFFD700);
        y += LINE_GAP;

        graphics.drawCenteredString(this.font,
                Component.literal("若想管理或者安装模组，请阅读以下教程："),
                this.width / 2, y, TEXT_COLOR);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    /**
     * 关闭本页（Esc）等同于点「跳过」。
     * <p>首次启动流程里不能把玩家留在半路：Esc 也必须走到同一处收尾。
     */
    @Override
    public void onClose() {
        finish();
    }
}
