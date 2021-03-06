// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.containers.Stack;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.Angular2ModuleResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.angular2.Angular2DecoratorUtil.getProperty;

public class Angular2SourceModule extends Angular2SourceEntity implements Angular2Module {

  private final Angular2ModuleResolver<ES6Decorator> myModuleResolver = new Angular2ModuleResolver<>(
    this::getDecorator, Angular2SourceModule::collectSymbols);

  public Angular2SourceModule(@NotNull ES6Decorator decorator,
                              @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @Override
  @NotNull
  public Set<Angular2Declaration> getDeclarations() {
    return myModuleResolver.getDeclarations();
  }

  @Override
  @NotNull
  public Set<Angular2Module> getImports() {
    return myModuleResolver.getImports();
  }

  @Override
  @NotNull
  public Set<Angular2Entity> getExports() {
    return myModuleResolver.getExports();
  }

  @NotNull
  @Override
  public Set<Angular2Declaration> getAllExportedDeclarations() {
    return myModuleResolver.getAllExportedDeclarations();
  }

  @Override
  public boolean isScopeFullyResolved() {
    return myModuleResolver.isScopeFullyResolved();
  }

  @Override
  public boolean areExportsFullyResolved() {
    return myModuleResolver.areExportsFullyResolved();
  }

  @Override
  public boolean areDeclarationsFullyResolved() {
    return myModuleResolver.areDeclarationsFullyResolved();
  }

  @Override
  public boolean isPublic() {
    //noinspection HardCodedStringLiteral
    return !getName().startsWith("ɵ");
  }

  private static <T extends Angular2Entity> Pair<Set<T>, Boolean> collectSymbols(@NotNull ES6Decorator decorator,
                                                                                 @NotNull String propertyName,
                                                                                 @NotNull Class<T> symbolClazz) {
    JSProperty property = getProperty(decorator, propertyName);
    if (property == null) {
      return Pair.pair(Collections.emptySet(), true);
    }
    return AstLoadingFilter.forceAllowTreeLoading(property.getContainingFile(),
                                                  () -> new SourceSymbolCollector<>(symbolClazz).collect(property.getValue()));
  }

  private static class SourceSymbolCollector<T extends Angular2Entity> extends Angular2SourceEntityListProcessor<T> {

    private boolean myIsFullyResolved = true;
    private final Set<T> myResult = new HashSet<>();
    private final Stack<PsiElement> myResolveQueue = new Stack<>();

    SourceSymbolCollector(@NotNull Class<T> entityClass) {
      super(entityClass);
    }

    public Pair<Set<T>, Boolean> collect(@Nullable JSExpression value) {
      if (value == null) {
        return Pair.pair(myResult, false);
      }
      myResolveQueue.push(value);
      while (!myResolveQueue.empty()) {
        PsiElement element = myResolveQueue.pop();
        List<PsiElement> children = resolve(element);
        if (children.isEmpty()) {
          element.accept(getResultsVisitor());
        }
        else {
          myResolveQueue.addAll(children);
        }
      }
      return Pair.pair(myResult, myIsFullyResolved);
    }

    @Override
    protected void processNonEntityClass(@NotNull JSClass aClass) {
      myIsFullyResolved = false;
    }

    @Override
    protected void processEntity(@NotNull T entity) {
      myResult.add(entity);
    }

    @Override
    protected void processAnyType() {
      myIsFullyResolved = false;
    }

    @Override
    protected void processAnyElement(JSElement node) {
      myIsFullyResolved = false;
    }
  }
}
