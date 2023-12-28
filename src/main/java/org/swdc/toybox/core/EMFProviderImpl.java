package org.swdc.toybox.core;

import org.swdc.data.EMFProvider;
import org.swdc.toybox.core.entity.IndexFolder;

import java.util.Arrays;
import java.util.List;

public class EMFProviderImpl extends EMFProvider {
    @Override
    public List<Class> registerEntities() {
        return Arrays.asList(
            IndexFolder.class
        );
    }
}
