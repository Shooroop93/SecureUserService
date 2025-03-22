package com.secureuser.service.config;

import com.secureuser.service.grpc.AuthServiceImpl;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Bean
    public GrpcServerConfigurer serverConfigurer(AuthServiceImpl authServiceImpl) {
        return serverBuilder -> serverBuilder.addService(authServiceImpl);
    }
}