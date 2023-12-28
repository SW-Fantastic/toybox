package org.swdc.toybox.views;

import org.swdc.dependency.annotations.ImplementBy;
import org.swdc.toybox.views.previews.ArchivePreviewModal;
import org.swdc.toybox.views.previews.ImagePreviewModal;
import org.swdc.toybox.views.previews.MusicPreviewModal;
import org.swdc.toybox.views.previews.TextPreviewModal;

import java.io.File;

@ImplementBy({
        ImagePreviewModal.class,
        TextPreviewModal.class,
        MusicPreviewModal.class,
        ArchivePreviewModal.class
})
public interface FilePreviewer {

    void preview(File file);

    boolean support(File file);

}
