package org.swdc.toybox.core;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.toybox.core.events.KeyShortcutRefreshEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class NativeKeyTrigger implements NativeKeyListener {

    @Inject
    private Logger logger;

    private Deque<Integer> pressedChain = new ArrayDeque<>();

    private Map<String,KeyShortcut> triggers = new HashMap<>();

    private List<Integer> effectKeys = new ArrayList<>();

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
        if (!effectKeys.contains(nativeEvent.getKeyCode())) {
            return;
        }
        pressedChain.addFirst(nativeEvent.getKeyCode());
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {

        int[] codes = pressedChain.stream()
                .mapToInt(i -> i)
                .toArray();

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
            if (!effectKeys.contains(cds[idx])) {
                effectKeys.add(cds[idx]);
            }
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
