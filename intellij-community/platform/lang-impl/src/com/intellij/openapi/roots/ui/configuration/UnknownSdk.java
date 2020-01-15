// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.roots.ui.configuration;

import com.google.common.base.Predicates;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Contains information and requirements for an SDK to resolve
 * with {@link UnknownSdkResolver} extension
 */
public interface UnknownSdk {
  /**
   * Type of SDK to resolve with {@link UnknownSdkResolver.UnknownSdkLookup}
   */
  @NotNull
  SdkType getSdkType();

  /**
   * A missing SDK name, if known. A detector implementation may rely on the naming
   * to use it for a better decision on the SDK to suggest.
   */
  @Nullable
  default String getSdkName() { return null; }

  /**
   * Condition to test a possible SDK candidates to match version
   * predicate. We use {@link SdkType#getVersionString(Sdk)} or
   * {@link Sdk#getVersionString()} to get versions in most of the cases.
   * <br/>
   * Use the {@link SdkType#versionComparator()} to better deal with versions comparison
   */
  @NotNull
  default Predicate<String> getSdkVersionStringPredicate() {
    return Predicates.alwaysTrue();
  }
}
