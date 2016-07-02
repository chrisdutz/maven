package org.apache.maven.feature;

import java.util.Arrays;
import java.util.List;

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

/**
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmarbaise@apache.org</a>
 *
 * @since 3.4.0
 */
public enum AvailableFeatureToggles
{

    //FIXME: Only some examples given. Nothing which exists in reality.

//    MNG9991( "MNG-9991", "First Feature to be toggable via command line option. "
//        + "First Feature to be toggable via command line option." ),
//    MNG9992( "MNG-9992", "First Feature to be toggable via command line option. "
//        + "First Feature to be toggable via command line option. XX asdfa. asdf dsf." ),
//    MNG9993( "MNG-9993", "First Feature to be toggable via command line option. "
//        + "More text than you think." ),
//    MNG10000( "MNG-10000", "First Feature to be toggable via command line option. "
//        + "Here much more than you thing." ),

    /**
     * This is an feature toggle which will never being used nor does it exist.
     * This is only to mark the end of feature toggles. Also used for unit tests.
     * 
     * Keep it at the end.
     */
    UNKNOWN ("UNKNOWN", "The unknown feature.");

    private String issue;

    private String description;

    private AvailableFeatureToggles( String issue, String description )
    {
        this.issue = issue;
        this.description = description;
    }

    public static AvailableFeatureToggles[] getAvailableFeatureToggles() {
        List<AvailableFeatureToggles> asList = Arrays.asList( AvailableFeatureToggles.values());

        AvailableFeatureToggles[] result = new AvailableFeatureToggles[asList.size() - 1];
        
        for ( int i = 0; i < asList.size() - 1; i++ )
        {
            if (asList.get( i ) != UNKNOWN) {
                result[i] = asList.get( 0 );
            }
        }
        return result;
    }
    
    public String getDescription()
    {
        return this.description;
    }

    public String getIssue()
    {
        return this.issue;
    }
}
