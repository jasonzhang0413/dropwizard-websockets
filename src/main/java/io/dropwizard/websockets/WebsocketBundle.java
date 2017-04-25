/**
 * The MIT License
 * Copyright (c) 2017 LivePerson, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.dropwizard.websockets;

import com.google.common.base.Optional;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.metrics.jetty9.websockets.InstWebSocketServerContainerInitializer;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static io.dropwizard.websockets.GeneralUtils.rethrow;

public class WebsocketBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private final Collection<ServerEndpointConfig> endpointConfigs = new ArrayList<>();
    private static final Logger LOG = LoggerFactory.getLogger(WebsocketBundle.class);
    volatile boolean starting = false;
    private ServerEndpointConfig.Configurator defaultConfigurator;
    public static final WebsocketConfiguration DEFAULT_CONFIG = new WebsocketConfiguration();


    public WebsocketBundle(ServerEndpointConfig.Configurator defaultConfigurator, Class<?>... endpoints) {
        this(defaultConfigurator, Arrays.asList(endpoints), new ArrayList<>());
    }

    public WebsocketBundle(Class<?>... endpoints) {
        this(null, Arrays.asList(endpoints), new ArrayList<>());
    }

    public WebsocketBundle(ServerEndpointConfig... configs) {
        this(null, new ArrayList<>(), Arrays.asList(configs));
    }

    public WebsocketBundle(ServerEndpointConfig.Configurator defaultConfigurator, Collection<Class<?>> endpointClasses, Collection<ServerEndpointConfig> serverEndpointConfigs) {
        this.defaultConfigurator = defaultConfigurator;
        endpointClasses.forEach((clazz)-> addEndpoint(clazz));
        this.endpointConfigs.addAll(serverEndpointConfigs);
    }

    public void addEndpoint(ServerEndpointConfig epC) {
        endpointConfigs.add(epC);
        if (starting)
            throw new RuntimeException("can't add endpoint after starting lifecycle");
    }

    protected WebsocketConfiguration getWebsocketConfiguration(T configuration) {
        if (configuration instanceof WebsocketBundleConfiguration) {
            return ((WebsocketBundleConfiguration) configuration).getWebsocketConfiguration();
        } else {
            return DEFAULT_CONFIG;
        }
    }

    public void addEndpoint(Class<?> clazz) {
        ServerEndpoint anno = clazz.getAnnotation(ServerEndpoint.class);
        if(anno == null){
            throw new RuntimeException(clazz.getCanonicalName()+" does not have a "+ServerEndpoint.class.getCanonicalName()+" annotation");
        }
        ServerEndpointConfig.Builder bldr =  ServerEndpointConfig.Builder.create(clazz, anno.value());
        if(defaultConfigurator != null){
            bldr = bldr.configurator(defaultConfigurator);
        }
        endpointConfigs.add(bldr.build());
        if (starting)
            throw new RuntimeException("can't add endpoint after starting lifecycle");
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(T configuration, Environment environment) {
        final WebsocketConfiguration websocketConfiguration = getWebsocketConfiguration(configuration);
        if(websocketConfiguration == null) {
            throw new RuntimeException("You need to provide an implementation of WebsocketBundleConfigurationInterface");
        }
        environment.lifecycle().addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {

            @Override
            public void lifeCycleStarting(LifeCycle event) {
                starting = true;
                try {
                    ServerContainer wsContainer = InstWebSocketServerContainerInitializer.
                            configureContext(environment.getApplicationContext(), environment.metrics());

                    setWebsocketConfiguration(wsContainer, websocketConfiguration);

                    StringBuilder sb = new StringBuilder("Registering websocket endpoints: ")
                            .append(System.lineSeparator())
                            .append(System.lineSeparator());
                    endpointConfigs.forEach(rethrow(conf -> addEndpoint(wsContainer, conf, sb)));
                    LOG.info(sb.toString());
                } catch (ServletException ex) {
                    throw new RuntimeException(ex);
                }
            }

            private void addEndpoint(ServerContainer wsContainer, ServerEndpointConfig conf, StringBuilder sb) throws DeploymentException {
                wsContainer.addEndpoint(conf);
                sb.append(String.format("    WS      %s (%s)", conf.getPath(), conf.getEndpointClass().getName())).append(System.lineSeparator());
            }

            private void setWebsocketConfiguration(ServerContainer serverContainer, WebsocketConfiguration websocketConfiguration) {
                Optional<Duration> idleTimeout = Optional.fromNullable(websocketConfiguration.getMaxSessionIdleTimeout());
                if (idleTimeout.isPresent()) {
                    serverContainer.setDefaultMaxSessionIdleTimeout(idleTimeout.get().toMilliseconds());
                }
                Optional<Duration> asyncTimeout = Optional.fromNullable(websocketConfiguration.getAsyncSendTimeout());
                if (asyncTimeout.isPresent()) {
                    serverContainer.setAsyncSendTimeout(asyncTimeout.get().toMilliseconds());
                }
                Optional<Size> binarySize = Optional.fromNullable(websocketConfiguration.getMaxBinaryMessageBufferSize());
                if (binarySize.isPresent()) {
                    serverContainer.setDefaultMaxBinaryMessageBufferSize((int) binarySize.get().toBytes());
                }
                Optional<Size> textSize = Optional.fromNullable(websocketConfiguration.getMaxTextMessageBufferSize());
                if (textSize.isPresent()) {
                    serverContainer.setDefaultMaxTextMessageBufferSize((int) textSize.get().toBytes());
                }
            }
        });
    }

}
