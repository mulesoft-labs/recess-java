package org.mule.tools.recess;

import java.util.Map;

public class Recess {
    private RecessWrapper recessWrapper;
    public Recess(String [] files, Map<String, Object> recessConfig)  {
        recessWrapper = new RecessWrapper(files, recessConfig, "hello_world.css");
    }

    public void run() {
        recessWrapper.runRecess();
    }
}
