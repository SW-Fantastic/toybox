package org.swdc.toybox.core.events;

import org.swdc.dependency.event.AbstractEvent;

/**
 * Index初始化的时候释放此事件。
 */
public class IndexerInitializeEvent extends AbstractEvent {
    public IndexerInitializeEvent() {
        super(null);
    }

}
