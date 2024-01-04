package org.swdc.toybox.extension.fsmapper.entity;

import org.swdc.dependency.event.AbstractEvent;

public class FileUpdateEvent extends AbstractEvent {

    public FileUpdateEvent(MappedFile file) {
        super(file);
    }

    @Override
    public MappedFile getMessage() {
        return super.getMessage();
    }
}
