package org.mule.tools.recess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.tools.shell.Global;
import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.api.Runnable;
import org.mule.tools.rhinodo.impl.JavascriptRunner;
import org.mule.tools.rhinodo.impl.NodeModuleFactoryImpl;
import org.mule.tools.rhinodo.impl.NodeModuleImpl;
import sun.org.mozilla.javascript.internal.BaseFunction;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.Undefined;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class RecessWrapper implements Runnable {
    private NodeModuleImpl recess;
    private JavascriptRunner javascriptRunner;
    private Map<String, Object> config;
    private String[] files;
    private final String outputFile;

    public RecessWrapper(String[] files, Map<String, Object> config, String outputFile) {
        if ( config == null ) {
            throw new RuntimeException("config cannot be null");
        }
        this.files = files;
        this.config = config;
        this.outputFile = outputFile;

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

    public void runRecess() {
        javascriptRunner.run();
    }

    @Override
    public void run(Context ctx, Global global) {
        global.put("__dirname", global, Context.toString(recess.getPath().getPath()));

        ObjectMapper objectMapper = new ObjectMapper();

        URI sample = JavascriptRunner.getURIFromResources(this.getClass(), "npm.css");

        Function require = (Function)global.get("require", global);
        Object result = require.call(ctx,global,global,new String [] {"recess"});

        NativeObject nobj = new NativeObject();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            nobj.defineProperty(Context.toString(entry.getKey()), Context.javaToJS(entry.getValue(),nobj), NativeObject.READONLY);
        }

        Function recess = (Function) Context.jsToJava(result, Function.class);
        recess.call(ctx,global,global,new Object[] {Context.toString(sample.getPath()), nobj, new BaseFunction() {
            @Override
            public Object call(sun.org.mozilla.javascript.internal.Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                NativeObject err = objects.length > 0 ? (NativeObject) objects[0] : null;
                String result = objects.length > 1 ? (String) objects[1] : null;
                callback(err,result);
                System.out.println("res" + result);
                return Undefined.instance;
            }

            private void callback(NativeObject error, String result) {
                try {
                    FileUtils.write(new File(outputFile), result);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }});

    }

}
