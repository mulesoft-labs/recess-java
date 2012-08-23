package org.mule.tools.recess;

import org.junit.Test;
import org.mule.tools.rhinodo.impl.JavascriptRunner;

import java.net.URI;
import java.util.HashMap;

public class RecessWrapperTest {
    @Test
    public void testRunRecess() throws Exception {
        URI css = JavascriptRunner.getURIFromResources(this.getClass(), "npm.css");
        HashMap<String, Object> config = new HashMap<String, Object>();
//        config.put("cli", false);
        config.put("compile", true);
        new Recess(new String[] {"asd"}, config).run();
    }
}
