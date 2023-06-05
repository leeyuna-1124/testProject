package com.example.testproject;

import com.github.dockerjava.api.model.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.api.DockerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RestController
public class DockerBuilderClientTest {
    private final Logger LOGGER = LoggerFactory.getLogger(DockerBuilderClientTest.class);
    @GetMapping( "/listImages")
    public List<Image> listImages() {

        DockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://192.168.0.205:2375")
                .withDockerTlsVerify(false)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .maxConnections(100)
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(dockerClientConfig, httpClient);

        List<Image> imageList = dockerClient.listImagesCmd().exec();

//        LOGGER.info("ImageList : {}", imageList);

        return imageList;
    }
}
