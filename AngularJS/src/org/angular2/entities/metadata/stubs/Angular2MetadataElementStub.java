// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.metadata.psi.MetadataElement;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.angular2.lang.metadata.stubs.MetadataElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public abstract class Angular2MetadataElementStub<Psi extends MetadataElement> extends MetadataElementStub<Psi> {

  private static final AtomicNotNullLazyValue<Map<String, ConstructorFromJsonValue>> TYPE_FACTORY =
    new AtomicNotNullLazyValue<Map<String, ConstructorFromJsonValue>>() {
      @NotNull
      @Override
      protected Map<String, ConstructorFromJsonValue> compute() {
        return ContainerUtil.<String, ConstructorFromJsonValue>immutableMapBuilder()
          .put("class", Angular2MetadataClassStubBase::createClassStub)
          .put("reference", Angular2MetadataReferenceStub::createReferenceStub)
          .put("function", Angular2MetadataFunctionStub::createFunctionStub)
          .put(ARRAY_TYPE, Angular2MetadataArrayStub::new)
          .put(OBJECT_TYPE, Angular2MetadataObjectStub::new)
          .build();
      }
    };

  public Angular2MetadataElementStub(@Nullable String memberName, @Nullable StubElement parent, @NotNull MetadataElementType elementType) {
    super(memberName, parent, elementType);
  }

  public Angular2MetadataElementStub(@NotNull StubInputStream stream,
                                     @Nullable StubElement parent,
                                     @NotNull MetadataElementType elementType)
    throws IOException {
    super(stream, parent, elementType);
  }

  @Override
  protected Map<String, ConstructorFromJsonValue> getTypeFactory() {
    return TYPE_FACTORY.getValue();
  }
}
