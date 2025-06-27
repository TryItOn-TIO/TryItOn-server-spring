import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // 1. /actuator/health 엔드포인트는 인증 없이 모두에게 허용
                        .requestMatchers(EndpointRequest.to("health")).permitAll()

                        // 2. (선택사항) 다른 모든 actuator 엔드포인트는 인증된 사용자에게만 허용
                        // .requestMatchers(EndpointRequest.toAnyEndpoint()).authenticated()

                        // 3. 그 외 다른 모든 요청에 대한 규칙 (매우 중요!)
                        //    - 현재는 모든 요청을 허용하도록 설정되어 있습니다.
                        //    - 실제 프로젝트의 보안 요구사항에 맞게 이 부분을 수정해야 합니다.
                        //      (예: .anyRequest().authenticated() 로 변경하여 로그인 요구)
                        .anyRequest().permitAll()
                );
        // .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (필요 시)
        // .formLogin(...) // 폼 로그인 설정 (필요 시)

        return http.build();
    }
}