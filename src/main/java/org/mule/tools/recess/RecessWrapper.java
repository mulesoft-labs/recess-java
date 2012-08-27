package org.mule.tools.recess;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;
import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.api.Runnable;
import org.mule.tools.rhinodo.impl.JavascriptRunner;
import org.mule.tools.rhinodo.impl.NodeModuleFactoryImpl;
import org.mule.tools.rhinodo.impl.NodeModuleImpl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class RecessWrapper implements Runnable {
    private NodeModuleImpl recess;
    private JavascriptRunner javascriptRunner;
    private Map<String, Object> config;
    private Object[] files;
    private final String outputFile;
    private boolean merge;

    public RecessWrapper(Object[] files, Map<String, Object> config, String outputFile, boolean merge) {
        if ( config == null ) {
            throw new RuntimeException("config cannot be null");
        }
        this.files = files;
        this.config = config;
        this.outputFile = outputFile;
        this.merge = merge;

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

        Function require = (Function)global.get("require", global);
        Object result = require.call(ctx,global,global,new String [] {"recess"});

        NativeObject nobj = new NativeObject();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            nobj.defineProperty(entry.getKey(), Context.javaToJS(entry.getValue(),nobj), NativeObject.READONLY);
        }

        Function recess = (Function) Context.jsToJava(result, Function.class);

        recess.call(ctx,global,global,new Object[] {ctx.newArray(global, files), nobj, new BaseFunction() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                Scriptable err = objects.length > 0 ? (Scriptable) objects[0] : null;
                Scriptable result = objects.length > 1 ? (Scriptable) objects[1] : null;
                callback(err,result);
                return Undefined.instance;
            }

            private void callback(Scriptable error, Scriptable result) {
                if (error != null && error != Undefined.instance) {
                    if ( error instanceof NativeArray ) {
                        NativeArray errorAsNativeArray = (NativeArray) error;
                        for (Object o : errorAsNativeArray) {
                            if ( o instanceof NativeObject ) {
                                throw new RuntimeException(((NativeObject) o).get("message").toString());
                            }
                        }
                    }
                    throw new RuntimeException(error.toString());
                }

                if(result instanceof NativeArray) {
                    NativeArray resultAsNativeArray = (NativeArray) result;
                    for (Object o : resultAsNativeArray) {
                        writeOutputResult(o, merge);
                    }
                } else {
                    writeOutputResult(result, false);
                }
            }
        }});

    }

    private void writeOutputResult(Object document, boolean merge) {
        if (document == null || !(document instanceof NativeObject)) {
            return;
        }

        NativeObject documentObject = (NativeObject) document;

        NativeArray output = (NativeArray) documentObject.get("output", documentObject);

        String path = (String) documentObject.get("path", documentObject);

        String substring = path.substring(path.lastIndexOf(File.separator));
        if ( substring.endsWith(".css") || substring.endsWith(".less")) {
            substring = substring.substring(0, substring.lastIndexOf(".") - 1);
        }

        File outputFile;
        if ( merge ) {
            outputFile = new File(this.outputFile);
        } else {
            outputFile = new File(substring);
        }

        if (output.size() > 1) {
            try {
                FileUtils.write(outputFile, output.get(0).toString(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
