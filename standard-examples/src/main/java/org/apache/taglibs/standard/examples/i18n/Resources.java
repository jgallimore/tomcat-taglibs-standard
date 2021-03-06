/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.standard.examples.i18n;

import java.util.ListResourceBundle;

public class Resources extends ListResourceBundle {
    private static Object[][] contents;

    static {
        contents = new Object[][]{
                {"greetingMorning", "Good Morning!"},
                {"greetingEvening", "Good Evening!"},
                {"serverInfo", "Name/Version of Servlet Container: {0}, "
                        + "Java Version: {1}"},
                {"currentTime", "Current time: {0}"},
                {"com.acme.labels.cancel", "Cancel"},
                {"java.lang.ArithmeticException", "division by 0"}
        };
    }

    public Object[][] getContents() {
        return contents;
    }
}
