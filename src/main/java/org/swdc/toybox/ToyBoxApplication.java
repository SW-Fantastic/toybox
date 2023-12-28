package org.swdc.toybox;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import net.contentobjects.jnotify.JNotify;
import org.swdc.data.EMFProviderFactory;
import org.swdc.dependency.DependencyContext;
import org.swdc.dependency.EnvironmentLoader;
import org.swdc.fx.FXApplication;
import org.swdc.fx.FXResources;
import org.swdc.fx.SWFXApplication;
import org.swdc.toybox.core.EMFProviderImpl;
import org.swdc.toybox.core.NativeKeyTrigger;
import org.swdc.toybox.core.NativeKeyUtils;
import org.swdc.toybox.core.ext.BoxExtensionContext;
import org.swdc.toybox.views.ExtensionView;
import org.swdc.toybox.views.MainView;
import org.swdc.toybox.views.TrayView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ResourceBundle;

/**
 * 应用启动和依赖控制的类
 * @author SW-Fantastic
 */
@SWFXApplication(assetsFolder = "./assets",
        splash = SplashScreen.class,
        configs = { ApplicationConfig.class },
        icons = { "search-16.png","search-24.png","search-32.png","search-64.png","search-128.png","search-256.png" })
public class ToyBoxApplication extends FXApplication {

    private DependencyContext context;

    private void configJNotify(File assetFolder) {
        String osName = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        String ext = "";

        if (osName.contains("windows")) {
            osName = "windows";
            ext = "dll";
        } else if (osName.contains("linux")) {
            osName = "linux";
            ext = "so";
        } else if (osName.contains("mac")) {
            osName = "osx";
            ext = "jnilib";
        }
        if (arch.contains("64")) {
            arch = "64";
        } else if (osName.equals("osx")){
            arch = "";
        } else if (arch.contains("32") || arch.contains("86")) {
            arch = "32";
        }

        String jNotifyNAPI = "META-INF/native/" + osName + arch + "/jnotify_" + arch + "bit." + ext;
        String location = assetFolder.getAbsolutePath() +"/platform/jnotify/" + "jnotify_" + arch + "bit." + ext;
        File libraryFile = new File(location);
        if (!libraryFile.exists()) {
            File folder = libraryFile.getParentFile();
            if(!folder.exists() && !folder.mkdirs()) {
                logger.error("failed to create library folder, can not start application.");
                System.exit(0);
            }
            try {
                InputStream libraryStream = JNotify.class.getModule().getResourceAsStream(jNotifyNAPI);
                FileOutputStream fos = new FileOutputStream(libraryFile);
                libraryStream.transferTo(fos);
                fos.close();
                libraryStream.close();
            } catch (Exception e) {
                logger.error("failed to extract jnotify library.", e);
            }
        }
        JNotify.nativeLoadLibrary(libraryFile);
    }

    @Override
    public void onConfig(EnvironmentLoader loader) {
        loader.withProvider(EMFProviderImpl.class);
    }

    @Override
    public void onStarted(DependencyContext dependencyContext) {

        this.context = dependencyContext;

        FXResources resources = dependencyContext.getByClass(FXResources.class);
        configJNotify(resources.getAssetsFolder());

        EMFProviderFactory factory = dependencyContext.getByClass(EMFProviderFactory.class);
        factory.create();

        ApplicationConfig config = dependencyContext.getByClass(ApplicationConfig.class);
        NativeKeyTrigger trigger = dependencyContext.getByClass(NativeKeyTrigger.class);
        trigger.registerTrigger(
                NativeKeyUtils.keyShortcutId(config.getClass(),"extensionKey"),
                NativeKeyUtils.stringToKeyCode(config.getExtensionKey()), () -> Platform.runLater(() -> {
                    ExtensionView view = dependencyContext.getByClass(ExtensionView.class);
                    if(view.getStage().isShowing()) {
                        view.getStage().toFront();
                    } else {
                        view.show();
                    }
                })
        );

        ResourceBundle bundle = resources.getResourceBundle();
        MainView mainView = dependencyContext.getByClass(MainView.class);

        try {

            SystemTray tray = SystemTray.getSystemTray();
            TrayIcon icon = new TrayIcon(SwingFXUtils.fromFXImage(resources.getIcons().get(0),null));
            icon.setToolTip(bundle.getString(LangConstants.APP_NAME));
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        TrayView trayView = dependencyContext.getByClass(TrayView.class);
                        trayView.show(e);
                    } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                        mainView.show();
                    }
                }
            });
            tray.add(icon);

            mainView.show();

        } catch (Exception e) {
            logger.error("failed to register native hook.");
        }
    }

    public DependencyContext getContext() {
        return context;
    }
}
