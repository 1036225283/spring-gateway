package xws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * 程序启动入口
 * Created by root on 2018/7/16.
 */


//下面的注解激活Eureka中的DiscoveryClient实现自动化配置
@SpringBootApplication
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("path_route", r -> r.path("/get").uri("http://www.baidu.com"))
//                .route(p -> p.host("").filters(f -> f.hystrix(config -> config.setFallbackUri(""))).uri(""))
//                .route(predicateSpec -> predicateSpec.path("").filters(f -> f.hystrix(config -> config.setFallbackUri(""))).uri(""))
                .build();
    }

    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }


    @Bean
    public GlobalFilter customGlobalPostFilter() {
        return (exchange, chain) -> chain.filter(exchange)
                .then(Mono.just(exchange))
                .map(serverWebExchange -> {
                    //adds header to response
                    String token = serverWebExchange.getRequest().getHeaders().getFirst("token");
                    if (token == null) {
                        System.out.println("no token");
                        return serverWebExchange.getResponse().setComplete();
                    } else {
                        System.out.println("token = " + token);
                    }

                    serverWebExchange.getResponse().getHeaders().set("CUSTOM-RESPONSE-HEADER",
                            HttpStatus.OK.equals(serverWebExchange.getResponse().getStatusCode()) ? "It worked" : "It did not work");
                    return serverWebExchange;
                })
                .then();
    }
}
