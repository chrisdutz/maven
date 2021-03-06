package org.apache.maven.repository.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;

import javax.inject.Named;

/**
 * Default implementation of LanguageSupport, which makes the scope handling
 * for Java projects available.
 */
@Named
@Component( role = LanguageSupport.class, hint = "java" )
public class JavaLanguageSupport implements LanguageSupport
{

    private static final JavaScopeSelector SCOPE_SELECTOR = new JavaScopeSelector();
    private static final JavaScopeDeriver SCOPE_DERIVER = new JavaScopeDeriver();

    @Override
    public String getLanguageName()
    {
        return "java";
    }

    @Override
    public ConflictResolver.ScopeSelector getScopeSelector()
    {
        return SCOPE_SELECTOR;
    }

    @Override
    public ConflictResolver.ScopeDeriver getScopeDeriver()
    {
        return SCOPE_DERIVER;
    }

}
