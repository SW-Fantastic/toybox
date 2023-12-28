package org.swdc.toybox.views.previews;

import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.swdc.dependency.annotations.MultipleImplement;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.toybox.views.FilePreviewer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@MultipleImplement(FilePreviewer.class)
@View(viewLocation = "views/previewer/MusicPreviewerModal.fxml",resizeable = false)
public class MusicPreviewModal extends AbstractView implements FilePreviewer {

    private List<String> extensions = Arrays.asList(
            "mp3","m4a","wma","wav"
    );

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Override
    public void preview(File file) {
        Stage stage = getStage();
        stage.setTitle(file.getName());
        stage.setAlwaysOnTop(true);

        MusicPreviewController controller = getController();
        controller.doPreview(file);
    }

    @Override
    public boolean support(File file) {
        String name = file.getName().toLowerCase();
        int lastIdx = name.lastIndexOf('.');
        String ext = name.substring(lastIdx + 1);
        return extensions.contains(ext);
    }

    public void changeVolume(boolean mute) {
        Button volBtn = findById("vol");
        volBtn.setPadding(new Insets(4));
        volBtn.setFont(fontawsome5Service.getSolidFont(FontSize.MIDDLE_SMALL));
        if (mute) {
            volBtn.setText(fontawsome5Service.getFontIcon("volume-mute"));
        } else {
            volBtn.setText(fontawsome5Service.getFontIcon("volume-down"));
        }
    }

    public void changePlayState(boolean isPlaying) {

        Button play = findById("btnPlay");
        play.setPadding(new Insets(4));
        play.setFont(fontawsome5Service.getSolidFont(FontSize.MIDDLE));
        if (isPlaying) {
            play.setText(fontawsome5Service.getFontIcon("pause"));
        } else {
            play.setText(fontawsome5Service.getFontIcon("play"));
        }
    }

}
