/*
 * Copyright 1999,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.standard.tag.el.fmt;

import javax.servlet.jsp.*;
import org.apache.cactus.*;
import org.apache.taglibs.standard.testutil.TestUtil;

public class TestDateTag extends JspTestCase {

    public TestDateTag(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDateTag() throws Exception {
        String var = "varDate";
        String toInclude = TestUtil.getTestJsp(this);
        pageContext.include(toInclude);
        String actual = (String) pageContext.getAttribute(var, PageContext.APPLICATION_SCOPE);

        // This isn't desired; Cactus doesn't set things up right
        assertEquals(new java.util.Date().toString().length(), actual.length());
        
        // Ideally we would run this, or something like it
//        assertEquals("yyyy-MM-dd".length(), actual.length());
    }
}
