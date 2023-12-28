package org.swdc.toybox.views.previews;

import javafx.stage.Stage;
import org.swdc.dependency.annotations.MultipleImplement;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.toybox.views.FilePreviewer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@MultipleImplement(FilePreviewer.class)
@View(viewLocation = "views/previewer/TextPreviewModal.fxml")
public class TextPreviewModal extends AbstractView implements FilePreviewer{

    private List<String> extensions = Arrays.asList(
            "txt","md","java","cpp","c","css","less","html",
            "htm","sass","js","ts","ini","conf","xml","h",
            "json"
    );

    @Override
    public void preview(File file) {
        Stage stage = getStage();
        stage.setAlwaysOnTop(true);
        stage.setTitle(file.getName());

        TextPreviewController controller = getController();
        controller.doPreview(file);
    }

    @Override
    public boolean support(File file) {
        String name = file.getName().toLowerCase();
        int lastIdx = name.lastIndexOf('.');
        String ext = name.substring(lastIdx + 1);
        return extensions.contains(ext);
    }
}
