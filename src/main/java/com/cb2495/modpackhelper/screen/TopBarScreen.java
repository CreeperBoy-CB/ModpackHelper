package com.cb2495.modpackhelper.screen;

import com.cb2495.modpackhelper.widget.ScreenTopBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 帮助页 / 常见问题页共用的骨架。
 * <p>本模组的界面都有同一套外观：顶部一条带遮罩的工具栏（左侧平台切换或分类页签、
 * 居中标题、右侧退出），下面是一块可滚动的正文。把「顶栏布局 + 遮罩绘制顺序」
 * 抽到这里，各页只负责正文和自己往顶栏里放什么按钮。
 * <p>顶栏按钮全部是原版 {@link net.minecraft.client.gui.components.Button}，
 * 外观与点击音效都由原版提供。
 */
public abstract class TopBarScreen extends Screen {

    /** 正文起始 Y：顶栏之下。 */
    protected static final int CONTENT_TOP = ScreenTopBar.HEIGHT + 4;
    /** 正文底部留白。 */
    protected static final int CONTENT_BOTTOM_OFFSET = 10;

    protected final ScreenTopBar topBar = new ScreenTopBar(this);

    protected TopBarScreen(Component title) {
        super(title);
    }

    /**
     * 往顶栏里添加按钮。
     * <p>{@link #init()} 在窗口缩放时会重跑，所以每次都必须先
     * {@link ScreenTopBar#clear()} 再添加，否则按钮会不断堆积。
     */
    protected abstract void rebuildTopBar();

    @Override
    protected void init() {
        super.init();
        this.topBar.clear();
        rebuildTopBar();
    }

    /**
     * 画顶部遮罩 + 工具栏。
     * <p>必须在正文与 {@code super.render} 之后调用：遮罩是半透明的，
     * 先画的文字会被压暗，看起来像重影。
     */
    protected void renderTopBar(GuiGraphics graphics, int mouseX, int mouseY) {
        this.topBar.render(graphics, this.font, this.title.getString(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 顶栏盖在正文之上：命中它就不该继续落到下面的链接或列表里
        if (button == 0 && this.topBar.mouseClicked(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    /** 退出本页：没有返回目标时直接丢掉界面。 */
    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }
}
