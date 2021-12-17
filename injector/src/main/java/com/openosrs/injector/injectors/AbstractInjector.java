/*
 * Copyright (c) 2019, Lucas <https://github.com/Lucwousin>
 * All rights reserved.
 *
 * This code is licensed under GPL3, see the complete license in
 * the LICENSE file in the root directory of this submodule.
 */
package com.openosrs.injector.injectors;

import com.google.common.base.Stopwatch;
import com.openosrs.injector.injection.InjectData;
import lombok.RequiredArgsConstructor;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

@RequiredArgsConstructor
public abstract class AbstractInjector implements Injector {

  protected static final Logger log = Logging.getLogger(AbstractInjector.class);
  protected final InjectData inject;
  private Stopwatch stopwatch;

  public void start() {
    stopwatch = Stopwatch.createStarted();
  }

  public final String getCompletionMsg() {
    return "finished in " + stopwatch.toString();
  }
}
