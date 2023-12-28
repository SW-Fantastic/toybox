package org.swdc.toybox.core.events;

import org.swdc.dependency.event.AbstractEvent;

/**
 * 在Index准备完毕后释放此事件。
 */
public class IndexerReadyEvent extends AbstractEvent {

    public IndexerReadyEvent() {
        super(null);
    }

}
