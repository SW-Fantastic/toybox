package org.swdc.toybox.core;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.scene.input.KeyCode;
import org.swdc.config.AbstractConfig;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 处理全局键盘监听的工具类
 */
public class NativeKeyUtils {


    /**
     * javafx KeyCode和Native KeyCode的对应关系
     */
    private static Map<KeyCode,Integer> nativeKeyMap = new HashMap<>();

    /**
     * Native KeyCode和javafx KeyCode的对应关系
     */
    private static Map<Integer, KeyCode> keyCodeNativeMap = new HashMap<>();

    /**
     * 生成用于Native按键监听的触发器ID
     *
     * 这个主要是因为我需要在SettingView中检测这些有键盘操作的配置，
     * 发现它们的存在后，当配置被保存的时候会按照这个id自动刷新监听器，以便于
     * 让它能够监听最新配置的按键。
     *
     * @param configClass 保存此按键的配置类
     * @param property 配置类的属性（@Property注解的Value）
     * @return
     */
    public static String keyShortcutId(Class<? extends AbstractConfig> configClass, String property) {
        return configClass.getName() + "#" + property;
    }

    /**
     * String转Native KeyCode
     * @param data keyCode序列，字符串序列，以逗号分割，每一项都是KeyCode的数字。
     * @return keyCode数组
     */
    public static Integer[] stringToKeyCode(String data) {
        return Stream.of(data.split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toList())
                .toArray(new Integer[0]);
    }

    public static String keyCodeToString(int[] codes) {
        Integer[] integers = Arrays.stream(codes)
                .boxed()
                .toArray(Integer[]::new);
        return keyCodeToString(integers);
    }

    public static String keyCodeToString(Integer[] codes) {
        return Stream.of(codes)
                .map(cd->cd + "")
                .reduce((cdA, cdB) -> cdA + "," + cdB)
                .orElse("");
    }

    /**
     * 初始化KeyCode的映射关系。
     */
    private static void initKeyMaps() {
        if (nativeKeyMap.isEmpty()) {
            for (KeyCode theCode : KeyCode.values()) {
                String nativeField = "VC_" + theCode.getName().toUpperCase().replace(" ", "_");
                if (theCode.isDigitKey()) {
                    String name = theCode.getName().toUpperCase();
                    if (name.startsWith("NUMPAD")) {
                        KeyCode target = KeyCode.getKeyCode(
                                name.replace("NUMPAD ","")
                        );
                        nativeField = "VC_" + target.getName().toUpperCase().replace(" ", "_");
                    }
                } else if (theCode.isModifierKey()) {
                    if (theCode == KeyCode.ALT_GRAPH) {
                        nativeField = "VC_ALT";
                    } else if (theCode == KeyCode.COMMAND || theCode == KeyCode.META) {
                        nativeField = "VC_META";
                    }else {
                        nativeField = "VC_" + theCode.name();
                    }
                }

                try {
                    Field field = NativeKeyEvent.class.getField(nativeField);
                    int nativeKey = field.getInt(null);
                    nativeKeyMap.put(theCode,nativeKey);
                    keyCodeNativeMap.put(nativeKey,theCode);
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * JavaFX KeyCode转Native KeyCode
     * @param code JavaFX KeyCode
     * @return Native key code
     */
    public static int getNativeKeyCode(KeyCode code) {
        if (nativeKeyMap.isEmpty() || keyCodeNativeMap.isEmpty()) {
            initKeyMaps();
        }
        return nativeKeyMap.get(code);
    }

    /**
     * Native KeyCode转JavaFx KeyCode
     * @param nativeCode Native KeyCode
     * @return JavaFX key code
     */
    public static KeyCode getKeyCodeFromNative(int nativeCode) {
        if (nativeKeyMap.isEmpty() || keyCodeNativeMap.isEmpty()) {
            initKeyMaps();
        }
        return keyCodeNativeMap.get(nativeCode);
    }

}
