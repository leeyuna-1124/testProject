package com.example.testproject;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.*;
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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;

@RestController
public class DockerBuilderClientTest {
    private final Logger LOGGER = LoggerFactory.getLogger(DockerBuilderClientTest.class);
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

    @GetMapping( "/listImages")
    public List<Image> listImages() {


        List<Image> imageList = dockerClient.listImagesCmd().exec();

        LOGGER.info("ImageList : {}", imageList);
        return imageList;
    }

    @GetMapping("/captureLogs")
    public String captureLogs() {
        // 컨테이너 설정
        String image = "inchang/ct_game:v2";
        String[] commands = {
                "/bin/bash",
                "-c",
                "chmod 755 /workspace/Build/Linux/MMORPG.x86_64 && mlagents-learn ./inference.yaml"
        };

        // 호스트 설정 (호스트 경로인 /home/xiness/xdocker/ctgame/app/workspace를 컨테이너 경로인 /workspace에 바인딩하도록 설정)
        Bind[] binds = {Bind.parse("/home/xiness/xdocker/ctgame/app/workspace" + ":" + "/workspace")};
        HostConfig hostConfig = HostConfig.newHostConfig().withBinds(binds).withPrivileged(true);

        // 컨테이너 생성
        CreateContainerResponse containerResponse = dockerClient.createContainerCmd(image)
                .withCmd(Arrays.asList(commands))
                .withHostConfig(hostConfig)
                .exec();

        String containerId = containerResponse.getId();

        // 컨테이너 실행
        dockerClient.startContainerCmd(containerId).exec();

        // 로그 캡처를 위한 ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 로그 캡처 실행
        ResultCallback<Frame> callback = new ResultCallback<Frame>() {
            @Override
            public void close() throws IOException {

            }

            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(Frame frame) {
                try {
                    outputStream.write(frame.getPayload());
                } catch (Exception e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                // 에러 처리
            }

            @Override
            public void onComplete() {
                // 캡처 완료 처리
            }
        };

        dockerClient.attachContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true)
                .exec(callback);

        // 일정 시간 동안 로그 캡처 대기
        try {
            Thread.sleep(5000); // 5초 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 캡처된 로그 출력
        String logs = outputStream.toString();
        LOGGER.info("LOGS!!!!!!!!!! : {}", logs);

        // 컨테이너 종료 대기
        dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback()).awaitStatusCode();

        //컨테이너 종료
        dockerClient.stopContainerCmd(containerId).exec();

        // 컨테이너 삭제
//        dockerClient.removeContainerCmd(containerId).exec();


        // Docker 클라이언트 종료
        try {
            dockerClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return logs;
    }
}
