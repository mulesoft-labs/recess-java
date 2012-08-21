package org.mule.tools.recess;

import org.junit.Test;
import org.mule.tools.rhinodo.impl.JavascriptRunner;

import java.net.URI;

public class RecessWrapperTest {
    @Test
    public void testRunRecess() throws Exception {
        URI css = JavascriptRunner.getURIFromResources(this.getClass(), "npm.css");
        new RecessWrapper().runRecess(css);
    }
}
