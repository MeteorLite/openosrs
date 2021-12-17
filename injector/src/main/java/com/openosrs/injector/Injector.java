/*
 * Copyright (c) 2019, Lucas <https://github.com/Lucwousin>
 * All rights reserved.
 *
 * This code is licensed under GPL3, see the complete license in
 * the LICENSE file in the root directory of this submodule.
 */
package com.openosrs.injector;

import com.openosrs.injector.injection.InjectData;
import com.openosrs.injector.injection.InjectTaskHandler;
import com.openosrs.injector.injectors.CreateAnnotations;
import com.openosrs.injector.injectors.InjectConstruct;
import com.openosrs.injector.injectors.InterfaceInjector;
import com.openosrs.injector.injectors.MixinInjector;
import com.openosrs.injector.injectors.RSApiInjector;
import com.openosrs.injector.injectors.raw.ClearColorBuffer;
import com.openosrs.injector.injectors.raw.CopyRuneLiteClasses;
import com.openosrs.injector.injectors.raw.DrawMenu;
import com.openosrs.injector.injectors.raw.GraphicsObject;
import com.openosrs.injector.injectors.raw.RasterizerAlpha;
import com.openosrs.injector.injectors.raw.RenderDraw;
import com.openosrs.injector.injectors.raw.RuneLiteIterables;
import com.openosrs.injector.injectors.raw.RuneliteMenuEntry;
import com.openosrs.injector.injectors.raw.RuneliteObject;
import com.openosrs.injector.injectors.raw.ScriptVM;
import com.openosrs.injector.net.DecodeNet;
import com.openosrs.injector.rsapi.RSApi;
import com.openosrs.injector.transformers.InjectTransformer;
import com.openosrs.injector.transformers.Java8Ifier;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.EnumConverter;
import net.runelite.asm.ClassFile;
import net.runelite.asm.ClassGroup;
import net.runelite.deob.util.JarUtil;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import static net.runelite.deob.util.JarUtil.load;

public class Injector extends InjectData implements InjectTaskHandler {

  static final Logger log = Logging.getLogger(Injector.class);
  static Injector injector = new Injector();
  static String oprsVer;

  public static void main(String[] args) {
    OptionParser parser = new OptionParser();

    ArgumentAcceptingOptionSpec<OutputMode> outModeOption =
        parser.accepts("outmode")
            .withRequiredArg().ofType(OutputMode.class)
            .withValuesConvertedBy(new EnumConverter<OutputMode>(OutputMode.class) {
              @Override
              public OutputMode convert(String value) {
                return super.convert(value.toUpperCase());
              }
            });

//    OptionSet options = parser.parse(args);
    oprsVer = "4.17.0";

    File clientMixins = new File("../runelite-mixins/build/libs/runelite-mixins-" + oprsVer + ".jar");
    if (clientMixins.exists()) {
      log.info("Injecting Client");

      injector.vanilla = load(
              new File("../runescape-client/build/libs/runescape-client-" + oprsVer + ".jar"));
      injector.deobfuscated = load(
              new File("../runescape-client/build/libs/runescape-client-" + oprsVer + ".jar"));
      injector.rsApi = new RSApi(Objects.requireNonNull(
              new File("../runescape-api/build/classes/java/main/net/runelite/rs/api/")
                      .listFiles()));
      injector.mixins = load(clientMixins);

      injector.initToVanilla();
      injector.injectVanilla();
      save(injector.getVanilla(), new File("../runelite-client/src/main/resources/injected-client.osrs"),
              null);
      save(injector.getVanilla(), new File("../runelite-client/build/injected/injected-client.osrs"),
              null);
    }
  }

  private static void save(ClassGroup group, File output, OutputMode mode) {
    if (output.exists()) {
      try {
        Files.walk(output.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
            .forEach(File::delete);
      } catch (IOException e) {
        log.info("Failed to delete output directory contents.");
        throw new RuntimeException(e);
      }
    }

    output.getParentFile().mkdirs();
    JarUtil.save(group, output);
  }

  private static void saveFiles(ClassGroup group, File outDir) {
    try {
      outDir.mkdirs();

      for (ClassFile cf : group.getClasses()) {
        File f = new File(outDir, cf.getName() + ".class");
        byte[] data = JarUtil.writeClass(group, cf);
        Files.write(f.toPath(), data);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void injectVanilla() {
    log.debug("[Starting injection]");

    transform(new Java8Ifier(this));

    inject(new CreateAnnotations(this));

    inject(new GraphicsObject(this));

    inject(new CopyRuneLiteClasses(this));

    inject(new RuneLiteIterables(this));

    inject(new RuneliteObject(this));

    //Injects initial RSAPI
    inject(new InterfaceInjector(this));

    inject(new RasterizerAlpha(this));

    inject(new MixinInjector(this));

    // This is where field hooks runs

    // This is where method hooks runs

    inject(new InjectConstruct(this));

    //Requires InterfaceInjector
    inject(new RSApiInjector(this));

    //Some annotations are still nice to have such as ObfName and ObfSig for Reflection checks
    //inject(new RemoveAnnotations(this));
    //The Reflection class is skipped during load because the asm doesnt support invokedynamic, ez fix to just put
    //it back in after doing everything
    JarUtil.addReflection(vanilla,
        new File("../runescape-client/build/libs/runescape-client-" + oprsVer + ".jar"));
    //inject(new DrawAfterWidgets(this));

    inject(new ScriptVM(this));

    // All GPU raw injectors should probably be combined, especially RenderDraw and Occluder
    inject(new ClearColorBuffer(this, HOOKS));

    inject(new RenderDraw(this, HOOKS));

    //inject(new Occluder(this));

    inject(new DrawMenu(this, HOOKS));

    inject(new DecodeNet(this));

//    inject(new RuneliteMenuEntry(this));

    //inject(new AddPlayerToMenu(this));

    //validate(new InjectorValidator(this));

    //transform(new SourceChanger(this));
  }

  private void inject(com.openosrs.injector.injectors.Injector injector) {
    final String name = injector.getName();

    //log.info(ANSI_YELLOW + "[Starting " + name + "]" + ANSI_RESET);

    injector.start();

    injector.inject();

    log.info(injector.getCompletionMsg());

    if (injector instanceof Validator) {
      validate((Validator) injector);
    }
  }

  private void validate(Validator validator) {
    final String name = validator.getName();

    if (!validator.validate()) {
      throw new InjectException(name + " failed validation");
    }
  }

  private void transform(InjectTransformer transformer) {
    final String name = transformer.getName();

    //log.info(ANSI_YELLOW + "Starting " + name + ANSI_RESET);

    transformer.transform();

    log.info(transformer.getCompletionMsg());
  }

  public void runChildInjector(com.openosrs.injector.injectors.Injector injector) {
    inject(injector);
  }
}
