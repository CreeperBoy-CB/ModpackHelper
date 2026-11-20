package com.cb2495.modpackhelper.client;

import com.cb2495.modpackhelper.help.HelpReadState;
import com.cb2495.modpackhelper.util.HelpImages;

import java.lang.reflect.Method;

/**
 * 从 VerityPlus（modid {@code verityconfig}）读取峰谷提示需要的两三个配置项。
 * <p>峰谷提示本身住在 Modpack Helper，但「用哪个 AI 提供商」与「要不要显示提示」
 * 这两个开关属于 Verity 的配置，只能在 VerityPlus 里读到。这里用反射取，
 * 是为了不把两个模组锁成循环编译依赖。
 * <p>取不到时一律按「不提示」处理：宁可少一条提醒，也不要因为反射失败刷屏报错。
 */
public final class VerityPlusConfigBridge {

    private VerityPlusConfigBridge() {}

    private static final String CONFIG_MANAGER = "com.cb2495.verityconfig.config.VerityConfigManager";
    private static final String MOD_CONFIG = "com.cb2495.verityconfig.config.ModConfig";

    /** 当前 AI 提供商名；取不到时返回空串。 */
    public static String currentProvider() {
        Object value = invoke(CONFIG_MANAGER, "getCurrentProvider");
        return value instanceof String s ? s : "";
    }

    /** 峰谷提示开关。 */
    public static boolean isHintEnabled() {
        Object value = invoke(MOD_CONFIG, "isShowDeepSeekHint");
        return value instanceof Boolean b && b;
    }

    /** 「怎么关」的说明是否已经在首次启动时讲过。 */
    public static boolean isPricingHintIntroduced() {
        Object value = invoke(MOD_CONFIG, "isPricingHintIntroduced");
        return value instanceof Boolean b && b;
    }

    /** 记下「已经讲过」的标记。 */
    public static void markPricingHintIntroduced() {
        invoke(MOD_CONFIG, "setPricingHintIntroduced", new Class<?>[]{boolean.class}, true);
    }

    /**
     * 反射调用 VerityPlus 的静态方法。
     * <p>失败只记一次 debug 日志：这条路径在玩家没装 VerityPlus 时必然失败，
     * 属于预期内的降级，不该刷警告。
     */
    private static Object invoke(String className, String method, Class<?>[] types, Object... args) {
        try {
            Class<?> cls = Class.forName(className);
            Method m = cls.getMethod(method, types);
            return m.invoke(null, args);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            HelpImages.debug("读取 VerityPlus 配置失败: " + className + "#" + method + " -> " + e);
            return null;
        }
    }

    private static Object invoke(String className, String method) {
        return invoke(className, method, new Class<?>[0]);
    }
}
