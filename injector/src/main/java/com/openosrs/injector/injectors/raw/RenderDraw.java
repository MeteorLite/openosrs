/*
 * Copyright (c) 2019, Lucas <https://github.com/Lucwousin>
 * All rights reserved.
 *
 * This code is licensed under GPL3, see the complete license in
 * the LICENSE file in the root directory of this submodule.
 */
package com.openosrs.injector.injectors.raw;

import com.openosrs.injector.InjectException;
import com.openosrs.injector.InjectUtil;
import com.openosrs.injector.injection.InjectData;
import com.openosrs.injector.injectors.AbstractInjector;
import net.runelite.asm.Method;
import net.runelite.asm.attributes.code.Instruction;
import net.runelite.asm.attributes.code.Instructions;
import net.runelite.asm.attributes.code.instructions.InvokeStatic;
import net.runelite.asm.attributes.code.instructions.InvokeVirtual;
import net.runelite.asm.pool.Class;
import net.runelite.asm.signature.Signature;

import java.util.ListIterator;

public class RenderDraw extends AbstractInjector {

  private  net.runelite.asm.pool.Method RENDERDRAW;
  private static final int EXPECTED = 21;

  public RenderDraw(InjectData inject, String hooks) {
    super(inject);
    RENDERDRAW = new net.runelite.asm.pool.Method(
            new Class(hooks),
            "renderDraw",
            new Signature("(Lnet/runelite/api/Renderable;IIIIIIIIJ)V"));
  }

  @Override
  public void inject() {
    int replaced = 0;

    /*
     * This class replaces entity draw invocation instructions
     * with the renderDraw method on drawcallbacks
     */
    final net.runelite.asm.pool.Method draw = InjectUtil
        .findMethod(inject, "draw", "Renderable", null, true, false).getPoolMethod();

    final Method drawTile = InjectUtil.findMethod(inject, "drawTile", "Scene", null, true, false);

    Instructions ins = drawTile.getCode().getInstructions();
    for (ListIterator<Instruction> iterator = ins.listIterator(); iterator.hasNext(); ) {
      Instruction i = iterator.next();
      if (i instanceof InvokeVirtual) {
        if (((InvokeVirtual) i).getMethod().equals(draw)) {
          iterator.set(new InvokeStatic(ins, RENDERDRAW));
          //			log.debug("[DEBUG] Replaced method call at {}", i);
          ++replaced;
        }
      }
    }

    if (replaced != EXPECTED) {
      throw new InjectException("Didn't replace the expected amount of method calls");
    }
  }
}
