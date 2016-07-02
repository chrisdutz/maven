package org.apache.maven.feature;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

@Component( instantiationStrategy = "singleton", role = SelectedFeatures.class )
public class DefaultSelectedFeatures
    implements SelectedFeatures
{
    @Requirement
    private Logger logger;

    private List<AvailableFeatures> activatedFeatures;

    @Override
    public void setActivatedFeatures( List<AvailableFeatures> featuresToBeActivated )
    {
        if ( featuresToBeActivated == null )
        {
            this.activatedFeatures = new ArrayList<>();
        }
        else
        {
            this.activatedFeatures = featuresToBeActivated;
        }
    }

    @Override
    public boolean isFeatureActive( AvailableFeatures feature )
    {
        if ( activatedFeatures != null )
        {
            return activatedFeatures.contains( feature );
        }
        return false;
    }

    @Override
    public List<AvailableFeatures> getActiveFeatures()
    {
        if ( activatedFeatures == null )
        {
            return Collections.emptyList();
        }
        else
        {
            return activatedFeatures;
        }
    }
    
}
