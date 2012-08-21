package org.mule.tools.recess;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.tools.shell.Global;
import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.api.Runnable;
import org.mule.tools.rhinodo.impl.JavascriptRunner;
import org.mule.tools.rhinodo.impl.NodeModuleFactoryImpl;
import org.mule.tools.rhinodo.impl.NodeModuleImpl;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

class RecessWrapper implements Runnable {
    private NodeModuleImpl recess;
    private JavascriptRunner javascriptRunner;
    private URI resource;

    public RecessWrapper() {
        recess = new NodeModuleImpl("META-INF/recess");

        List<? extends NodeModule> nodeModuleList = Arrays.asList(
                new NodeModuleImpl("META-INF/abbrev"),
                new NodeModuleImpl("META-INF/colors"),
                new NodeModuleImpl("META-INF/less"),
                new NodeModuleImpl("META-INF/nopt"),
                new NodeModuleImpl("META-INF/underscore"),
                new NodeModuleImpl("META-INF/watch"),
                recess);

        javascriptRunner = new JavascriptRunner(new NodeModuleFactoryImpl(nodeModuleList), this);
    }

    public void runRecess(URI resource) {
        this.resource = resource;
        javascriptRunner.run();
    }

    @Override
    public void run(Context ctx, Global global) {
        ctx.evaluateString(global,String.format("__dirname='%s';",recess.getPath().getPath()),"dirname",-1, null);

        URI sample = resource;

        String source = String.format(
                "var recess = require('recess'); " +
                        "recess('%s', {cli: true});", sample.getPath());

        ctx.evaluateString(global, source,"recess",-1,null);
    }

}
