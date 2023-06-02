package com.example.testproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
//import com.github.dockerjava.api.DockerClient;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

public class DockerBuilderClientTest {

    public static void main(String[] args) {

        final Logger LOGGER = LoggerFactory.getLogger(DockerBuilderClientTest.class);

        DockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.0.205:2375")
                .withDockerTlsVerify(false)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .maxConnections(100)
                .build();

        DockerClient doc = new DefaultDockerClient("http://192.168.0.205:2375");



//		DockerClient dockerClient = DockerClientImpl.getInstance(dockerClientConfig, httpClient);


//		dockerClient.pingCmd().exec();

        try {
            LOGGER.info("DockerClient : {}", doc.info());
        } catch (DockerException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
