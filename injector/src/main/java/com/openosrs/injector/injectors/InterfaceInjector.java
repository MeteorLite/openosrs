/*
 * Copyright (c) 2019, Lucas <https://github.com/Lucwousin>
 * All rights reserved.
 *
 * This code is licensed under GPL3, see the complete license in
 * the LICENSE file in the root directory of this submodule.
 */
package com.openosrs.injector.injectors;

import com.openosrs.injector.injection.InjectData;
import net.runelite.asm.ClassFile;
import net.runelite.asm.Interfaces;
import net.runelite.asm.pool.Class;
import net.runelite.deob.DeobAnnotations;
import static com.openosrs.injector.rsapi.RSApi.API_BASE;

public class InterfaceInjector extends AbstractInjector {

  private int implemented = 0;

  public InterfaceInjector(InjectData inject) {
    super(inject);
  }

  public void inject() {
    // forEachPair performs actions on a deob-vanilla pair, which is what's needed here
    inject.forEachPair(this::injectInterface);

    //log.info("[INFO] Injected {} interfaces", implemented);
  }

  private void injectInterface(final ClassFile deobCf, final ClassFile vanillaCf) {
    String impls = DeobAnnotations.getImplements(deobCf);

    if (impls == null) {
      return;
    }

    impls = impls.replace("", "");

    final String fullName = API_BASE + impls;
    if (fullName.contains("class"))
      return;

    String message = "[WARNING]" +
            " Class " + deobCf.getName() +
            " implements nonexistent interface: " + fullName + ", " +
                    "skipping interface injection...";
    if (!inject.getRsApi().hasClass(fullName)) {
      	log.debug(message);
      return;
    }

    final Interfaces interfaces = vanillaCf.getInterfaces();
    interfaces.addInterface(new Class(fullName));
    implemented++;

    inject.addToDeob(fullName, deobCf);
  }
}
