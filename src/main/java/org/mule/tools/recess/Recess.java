package org.mule.tools.recess;

import java.util.Map;

public class Recess {
    private RecessWrapper recessWrapper;
    public Recess(Object [] files, Map<String, Object> recessConfig, String outputFile, boolean merge)  {
        recessWrapper = new RecessWrapper(files, recessConfig, outputFile, merge);
    }

    public void run() {
        recessWrapper.runRecess();
    }
}
