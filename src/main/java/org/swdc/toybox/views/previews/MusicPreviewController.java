package org.swdc.toybox.views.previews;

import jakarta.inject.Inject;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.swdc.fx.view.ViewController;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MusicPreviewController extends ViewController<MusicPreviewModal> {

    @FXML
    private Label fileName;

    @FXML
    private Slider progress;

    @FXML
    private Slider volume;

    @Inject
    private Logger logger;

    private MediaPlayer player;

    private InvalidationListener progressChanged = v -> {
        if (player == null) {
            return;
        }
        double curr = player.getCurrentTime().toSeconds() / player.getTotalDuration().toSeconds();
        progress.setValue(curr * 100);
    };

    private SimpleBooleanProperty playing = new SimpleBooleanProperty();

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {
        MusicPreviewModal modal = getView();
        modal.getStage().getScene().setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (player != null) {
                    doReset();
                }
                getView().hide();
            } else if (e.getCode() == KeyCode.SPACE) {
                if (player != null && playing.getValue()) {
                    player.pause();
                } else if (player != null && !playing.getValue()) {
                    player.play();
                }
            }
        });

        modal.getStage().setOnHidden(e -> {
            doReset();
        });

        volume.valueProperty().addListener(v -> {
            double curr = volume.getValue() / 100.0;
            if (player != null) {
                player.setVolume(curr);
            }
            getView().changeVolume(curr == 0);
        });

        progress.setOnMouseReleased(this::doSeek);
        progress.setOnMouseDragged(this::doSeek);

        playing.addListener(v -> {
            getView().changePlayState(playing.getValue());
        });
    }

    private void doSeek(MouseEvent e) {
        double progressValue = progress.getValue() / 100.0;
        double pos = player.getTotalDuration().toSeconds() * progressValue;
        player.seek(new Duration(pos * 1000));
    }

    private void doReset() {

        if (player != null) {
            progress.valueProperty().unbindBidirectional(player.audioSpectrumIntervalProperty());
            player.currentTimeProperty().removeListener(this.progressChanged);
            player.stop();
            player.dispose();
            player = null;
        }

        playing.set(false);
        progress.setValue(0);

    }

    public void doPreview(File file) {
        try {

            if (this.player != null) {
                doReset();
            }

            Media media = new Media(file.toURI().toURL().toExternalForm());
            this.player = new MediaPlayer(media);
            this.player.setVolume(volume.getValue() / 100.0);
            this.player.currentTimeProperty().addListener(this.progressChanged);

            this.player.setOnReady(() -> {
                this.player.play();
            });
            this.player.setOnPlaying(() -> {
                if (!playing.getValue()) {
                    playing.setValue(true);
                }
            });

            this.player.setOnPaused(() -> {
                playing.set(false);
            });

            this.player.setOnEndOfMedia(() -> {
                this.player.seek(Duration.ZERO);
                this.player.pause();
            });

            fileName.setText(file.getName());
            getView().changeVolume(volume.getValue() == 0);
            getView().show();
        } catch (Exception e) {
            logger.error("failed on preview", e);
        }
    }

    @FXML
    public void onPause(){
        if (player == null) {
            doReset();
            getView().hide();
            return;
        }
        if (!playing.getValue()) {
            player.play();
        } else {
            player.pause();
        }
    }


    @FXML
    public void onMute() {
        if (player != null) {
            double vol = volume.getValue();
            if (vol > 0) {
                volume.setValue(0);
                player.setVolume(0);
            } else {
                volume.setValue(50);
                player.setVolume(0.5);
            }
            getView().changeVolume(volume.getValue() == 0);
        }
    }


}
