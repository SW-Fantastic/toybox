package org.swdc.toybox.extension;

import java.util.List;

public interface ExtensionContext {

    void register(Extension extension);

    List<Extension> getExtensions();

}
