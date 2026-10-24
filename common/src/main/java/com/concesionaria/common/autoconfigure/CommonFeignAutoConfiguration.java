package com.concesionaria.common.autoconfigure;

import com.concesionaria.common.feign.ErroresServicioDecoder;
import com.concesionaria.common.feign.FeignExceptionHandler;
import com.concesionaria.common.feign.TokenServicio;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

import javax.crypto.SecretKey;

/**
 * Solo en los servicios que usan Feign (tienen spring-cloud-starter-openfeign):
 * - cada llamada sale con un token de servicio (rol SISTEMA);
 * - los errores del otro servicio se traducen a las excepciones de common.
 */
@AutoConfiguration(after = CommonSeguridadAutoConfiguration.class)
@ConditionalOnClass(name = "feign.RequestInterceptor")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    TokenServicio tokenServicio(SecretKey jwtSecretKey, @Value("${spring.application.name}") String servicio) {
        return new TokenServicio(jwtSecretKey, servicio);
    }

    @Bean
    RequestInterceptor tokenServicioInterceptor(TokenServicio tokenServicio) {
        return plantilla -> plantilla.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenServicio.obtener());
    }

    @Bean
    @ConditionalOnMissingBean
    ErrorDecoder erroresServicioDecoder() {
        return new ErroresServicioDecoder();
    }

    @Bean
    FeignExceptionHandler feignExceptionHandler() {
        return new FeignExceptionHandler();
    }
}
