package org.ow2.proactive_grid_cloud_portal.rm.client;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ExceptionToJson;
import org.ow2.proactive_grid_cloud_portal.common.RMRestInterface;

import javax.net.ssl.SSLContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class RMRestClient {

    private RMRestInterface rm;

    private String restEndpointURL;

    private ResteasyProviderFactory providerFactory;

    private static ClientHttpEngine httpEngine;

    private static SSLContext sslContext;

    public RMRestClient(String restEndpointURL, ClientHttpEngine httpEngine) {

        if (httpEngine == null) {
            setBlindTrustSSLContext();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);
            HttpClient httpClient = HttpClientBuilder.create().setSSLSocketFactory(sslConnectionSocketFactory).build();
            httpEngine = new ApacheHttpClient4Engine(httpClient);
        }

        this.httpEngine = httpEngine;
        this.restEndpointURL = restEndpointURL;

        providerFactory = ResteasyProviderFactory.getInstance();
        if (!providerFactory.isRegistered(RMRestClient.JacksonContextResolver.class)) {
            providerFactory.registerProvider(RMRestClient.JacksonContextResolver.class);
        }
//        registerGzipEncoding(providerFactory);

        rm = createRestProxy(providerFactory, restEndpointURL, httpEngine);
    }

    public RMRestInterface getRm() {
        return rm;
    }

    private void setBlindTrustSSLContext() {
        try {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Provider
    @Consumes({ MediaType.APPLICATION_JSON, "text/json" })
    @Produces({ MediaType.APPLICATION_JSON, "text/json" })
    public static class JacksonContextResolver implements ContextResolver<ObjectMapper> {
        @Override
        public ObjectMapper getContext(Class<?> objectType) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper;
        }
    }

    private static RMRestInterface createRestProxy(ResteasyProviderFactory provider, String restEndpointURL,
                                                   ClientHttpEngine httpEngine) {
        ResteasyClient client = buildResteasyClient(provider);
        ResteasyWebTarget target = client.target(restEndpointURL);
        RMRestInterface rmRestInterface = target.proxy(RMRestInterface.class);
        return createExceptionProxy(rmRestInterface);
    }

    private static ResteasyClient buildResteasyClient(ResteasyProviderFactory provider) {
        return new ResteasyClientBuilder().providerFactory(provider).httpEngine(httpEngine).build();
    }

    private static RMRestInterface createExceptionProxy(final RMRestInterface rmRestInterface) {
        return (RMRestInterface) Proxy.newProxyInstance(RMRestInterface.class.getClassLoader(),
                new Class[] { RMRestInterface.class },
                new RMRestClient.RestClientExceptionHandler(rmRestInterface));
    }


    private static class RestClientExceptionHandler implements InvocationHandler {

        private final RMRestInterface rm;

        public RestClientExceptionHandler(RMRestInterface rm) {
            this.rm = rm;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(rm, args);
            } catch (InvocationTargetException targetException) {
                if (targetException.getTargetException() instanceof WebApplicationException) {
                    WebApplicationException clientException = (WebApplicationException) targetException.getTargetException();
                    try {
                        ExceptionToJson json = clientException.getResponse().readEntity(ExceptionToJson.class);
                        // here we take the server side exception and recreate it on the client side
//                        throw rebuildServerSideException(json);
                    } catch (ProcessingException couldNotReadJsonException) {
                        // rethrow server side exception as runtime exception but do not transform it
                        throw clientException;
                    } catch (IllegalStateException couldNotReadJsonException) {
                        // rethrow server side exception as runtime exception but do not transform it
                        throw clientException;
                    }
                }
                // rethrow real exception as runtime (client side exception)
                throw new RuntimeException(targetException.getTargetException());
            }
        }
    }
}
