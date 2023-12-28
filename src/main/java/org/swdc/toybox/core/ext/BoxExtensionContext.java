package org.swdc.toybox.core.ext;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.dependency.DependencyContext;
import org.swdc.fx.FXResources;
import org.swdc.toybox.ToyBoxApplication;
import org.swdc.toybox.extension.Extension;
import org.swdc.toybox.extension.ExtensionContext;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Named("extension")
@Singleton
public class BoxExtensionContext implements ExtensionContext {

    private List<Extension> extensions = new ArrayList<>();

    private Logger logger = LoggerFactory.getLogger(BoxExtensionContext.class);

    private DependencyContext context;

    @Inject
    public BoxExtensionContext(ToyBoxApplication application, FXResources resources) {
        context = application.getContext();
        Path extension = resources.getAssetsFolder().toPath().resolve("extension");
        if (!Files.exists(extension)) {
            try {
                Files.createDirectories(extension);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            List<Path> extensions = Files.list(extension)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            for (Path ext: extensions) {
                doLoadLayer(ext);
            }
        } catch (IOException e) {
            logger.error("Failed to load extension, can not read folder", e);
        }

    }

    private void doLoadLayer(Path extension){
        ModuleFinder moduleFinder = ModuleFinder.of(extension);
        List<String> moduleName = moduleFinder.findAll().stream()
                .map(ModuleReference::descriptor)
                .map(ModuleDescriptor::name)
                .collect(Collectors.toList());

        Configuration configuration = ModuleLayer
                .boot()
                .configuration()
                .resolve(
                        moduleFinder,
                        ModuleFinder.of(),
                        moduleName
                );

        ModuleLayer extensionLayer = ModuleLayer.boot().defineModulesWithOneLoader(
                configuration,
                ClassLoader.getSystemClassLoader()
        );

        ServiceLoader<Extension> serviceLoader = ServiceLoader.load(
                extensionLayer, Extension.class
        );

        for (Extension ext: serviceLoader) {
            register(ext);
            logger.info("extension: " + ext.extensionName() + " was loaded.");
        }
    }


    @Override
    public void register(Extension extension) {
        extensions.add(extension);
        extension.registered(context);
    }

    @Override
    public List<Extension> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }

}
