package com.vip.vjkit.sonarvj;

import org.sonar.api.Plugin;

/**
 * Created by cloud.huang on 18/1/5.
 */
public class SonarPlugin implements Plugin {
    @Override
    public void define(Context context) {
        context.addExtension(SonarDefinition.class);
        context.addExtension(SonarCheckRegistrar.class);
    }
}
