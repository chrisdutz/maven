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

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.MetadataGeneratorFactory;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.util.artifact.DefaultArtifactTypeRegistry;
import org.eclipse.aether.util.graph.manager.ClassicDependencyManager;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaDependencyContextRefiner;
import org.eclipse.aether.util.graph.transformer.NearestVersionSelector;
import org.eclipse.aether.util.graph.transformer.SimpleOptionalitySelector;
import org.eclipse.aether.util.graph.traverser.FatArtifactTraverser;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A utility class to assist in setting up a Maven-like repository system. <em>Note:</em> This component is meant to
 * assist those clients that employ the repository system outside of an IoC container, Maven plugins should instead
 * always use regular dependency injection to acquire the repository system.
 *
 * @author Benjamin Bentmann
 */
public final class MavenRepositorySystemUtils
{

    private MavenRepositorySystemUtils()
    {
        // hide constructor
    }

    /**
     * Creates a new service locator that already knows about all service implementations included in this library. To
     * acquire a complete repository system, clients need to add some repository connectors for remote transfers.
     *
     * @return The new service locator, never {@code null}.
     */
    public static DefaultServiceLocator newServiceLocator()
    {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.addService( ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class );
        locator.addService( VersionResolver.class, DefaultVersionResolver.class );
        locator.addService( VersionRangeResolver.class, DefaultVersionRangeResolver.class );
        locator.addService( MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class );
        locator.addService( MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class );
        return locator;
    }

    /**
     * Creates a new Maven-like repository system session by initializing the session with values typical for
     * Maven-based resolution. In more detail, this method configures settings relevant for the processing of dependency
     * graphs, most other settings remain at their generic default value. Use the various setters to further configure
     * the session with authentication, mirror, proxy and other information required for your environment.
     *
     * @return The new repository system session, never {@code null}.
     */
    public static DefaultRepositorySystemSession newSession()
    {
        List<LanguageSupport> languageSupports = new ArrayList<>( 1 );
        languageSupports.add( new JavaLanguageSupport() );
        return newSession( languageSupports );
    }

    /**
     * Creates a new Maven-like repository system session by initializing the session with values typical for
     * Maven-based resolution. In more detail, this method configures settings relevant for the processing of dependency
     * graphs, most other settings remain at their generic default value. Use the various setters to further configure
     * the session with authentication, mirror, proxy and other information required for your environment.
     *
     * @param languageSupports list of LanguageSupport objects that provide the scope handling of multiple languages.
     * @return The new repository system session, never {@code null}.
     */
    public static DefaultRepositorySystemSession newSession( List<LanguageSupport> languageSupports )
    {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

        DependencyTraverser depTraverser = new FatArtifactTraverser();
        session.setDependencyTraverser( depTraverser );

        DependencyManager depManager = new ClassicDependencyManager();
        session.setDependencyManager( depManager );

        // TODO: Check this ...
        // Dependency filter that excludes all "test" and "provided" scoped dependencies,
        // all optionals as well as all excluded artifacts.
        DependencySelector depFilter =
            new AndDependencySelector( new ScopeDependencySelector( "test", "provided" ),
                                       new OptionalDependencySelector(), new ExclusionDependencySelector() );
        session.setDependencySelector( depFilter );

        // In order to support multiple languages to provide scope selection and derivation,
        // we check how many LanguageSupports are available, if only one is available we use
        // the ScopeSelector and ScopeDeriver directly. It more than one is available, all
        // ScopeSelectors and ScopeDeriver are wrapped in special multi-language wrappers.
        ConflictResolver.ScopeSelector scopeSelector = null;
        ConflictResolver.ScopeDeriver scopeDeriver = null;
        if ( languageSupports != null )
        {
            if ( languageSupports.size() == 1 )
            {
                scopeSelector = languageSupports.get( 0 ).getScopeSelector();
                scopeDeriver = languageSupports.get( 0 ).getScopeDeriver();
            }
            else if ( languageSupports.size() > 1 )
            {
                // Add all the supported languages to multi-language scope-selectors and scope-derivers.
                MultiLanguageScopeSelector multiLanguageScopeSelector = new MultiLanguageScopeSelector();
                multiLanguageScopeSelector.setLanguageSupports( languageSupports );
                MultiLanguageScopeDeriver multiLanguageScopeDeriver = new MultiLanguageScopeDeriver();
                multiLanguageScopeDeriver.setLanguageSupports( languageSupports );
                scopeSelector = multiLanguageScopeSelector;
                scopeDeriver = multiLanguageScopeDeriver;
            }
        }

        DependencyGraphTransformer transformer =
            new ConflictResolver( new NearestVersionSelector(), scopeSelector,
                                  new SimpleOptionalitySelector(), scopeDeriver );

        session.setDependencyGraphTransformer(
            new ChainedDependencyGraphTransformer( transformer, new JavaDependencyContextRefiner() ) );

        DefaultArtifactTypeRegistry stereotypes = new DefaultArtifactTypeRegistry();
        stereotypes.add( new DefaultArtifactType( "pom" ) );
        stereotypes.add( new DefaultArtifactType( "maven-plugin", "jar", "", "java" ) );
        stereotypes.add( new DefaultArtifactType( "jar", "jar", "", "java" ) );
        stereotypes.add( new DefaultArtifactType( "ejb", "jar", "", "java" ) );
        stereotypes.add( new DefaultArtifactType( "ejb-client", "jar", "client", "java" ) );
        stereotypes.add( new DefaultArtifactType( "test-jar", "jar", "tests", "java" ) );
        stereotypes.add( new DefaultArtifactType( "javadoc", "jar", "javadoc", "java" ) );
        stereotypes.add( new DefaultArtifactType( "java-source", "jar", "sources", "java", false, false ) );
        stereotypes.add( new DefaultArtifactType( "war", "war", "", "java", false, true ) );
        stereotypes.add( new DefaultArtifactType( "ear", "ear", "", "java", false, true ) );
        stereotypes.add( new DefaultArtifactType( "rar", "rar", "", "java", false, true ) );
        stereotypes.add( new DefaultArtifactType( "par", "par", "", "java", false, true ) );
        session.setArtifactTypeRegistry( stereotypes );

        session.setArtifactDescriptorPolicy( new SimpleArtifactDescriptorPolicy( true, true ) );

        // MNG-5670 guard against ConcurrentModificationException
        Properties sysProps = new Properties();
        for ( String key : System.getProperties().stringPropertyNames() )
        {
            sysProps.put( key, System.getProperty( key ) );
        }
        session.setSystemProperties( sysProps );
        session.setConfigProperties( sysProps );

        return session;
    }

}
