package org.swdc.toybox.core;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.toybox.ApplicationConfig;
import org.swdc.toybox.core.events.KeyShortcutRefreshEvent;
import org.swdc.toybox.views.MainView;

import java.awt.*;
import java.util.*;
import java.util.List;

public class NativeKeyTrigger implements NativeKeyListener {

    record KeyShortcut(int[] codes, NativeKeyTriggeredListener listener) {

        public boolean isMatched(int[] codes) {
            if (codes.length != this.codes.length) {
                return false;
            }
            for (int idx = 0 ; idx < this.codes.length; idx ++) {
                // 注意，传入的Codes是倒序的，所以需要倒着比较，
                // 0 对应 length - 1， 1 对应 length - 2这种。
                int maxPos = codes.length - 1;
                int revPos = maxPos - idx;
                if (codes[revPos] != this.codes[idx]) {
                    return false;
                }
            }
            return true;
        }

    }

    @Inject
    private ApplicationConfig config;

    @Inject
    private MainView mainView;

    @Inject
    private Logger logger;

    private boolean mainTriggered;

    private long mainTriggeredMillers;

    private Deque<Integer> pressedChain = new ArrayDeque<>();

    private Map<String,KeyShortcut> triggers = new HashMap<>();

    @PostConstruct
    public void initialize() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            logger.info("native hook is active.");
        } catch (Exception e) {
            logger.error("failed to register native hooks.", e);
        }
    }

    @EventListener(type = KeyShortcutRefreshEvent.class)
    public void KeyRefreshEvent(KeyShortcutRefreshEvent event) {
        String key = event.getKey();
        KeyShortcut shortcut = triggers.get(key);
        if (shortcut == null) {
            logger.info("key shortcut refresh failed - no such key : " + key);
            return;
        }
        NativeKeyTriggeredListener listener = shortcut.listener();
        removeTrigger(key);
        registerTrigger(key,event.getCodes(),listener);
        logger.info("key shortcut refreshed : " + key);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        pressedChain.addFirst(nativeEvent.getKeyCode());
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {

        int code = NativeKeyUtils.stringToKeyCode(config.getSearchKey())[0];
        if (nativeEvent.getKeyCode() == code) {
            if (mainTriggered) {
                if ((nativeEvent.getWhen() - mainTriggeredMillers) < 2000) {
                    mainView.hide();
                    EventQueue.invokeLater(() -> {
                        mainView.show();
                        mainView.getStage().toFront();
                        mainView.focusField();
                    });
                }
                mainTriggered = false;
                return;
            } else {
                mainTriggered = true;
                mainTriggeredMillers = nativeEvent.getWhen();
            }
        } else {
            mainTriggered = false;
        }

        int[] codes = pressedChain.stream()
                .mapToInt(i -> i).toArray();
        pressedChain.clear();
        for (KeyShortcut shortcut: triggers.values()) {
            if (shortcut.isMatched(codes)) {
                shortcut.listener().apply();
                break;
            }
        }
    }

    public void registerTrigger(String key, Integer[] codes, NativeKeyTriggeredListener listener) {
        int[] cds = new int[codes.length];
        for (int idx = 0; idx < codes.length; idx ++) {
            cds[idx] = codes[idx];
        }
        registerTrigger(key,cds,listener);
    }

    public void registerTrigger(String key, int[] codes, NativeKeyTriggeredListener listener) {
        triggers.put(key,new KeyShortcut(codes,listener));
    }

    public void removeTrigger(String key) {
        triggers.remove(key);
    }

}
