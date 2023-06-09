package son.roomshare.config.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {


    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final TokenProvider tokenProvider;


    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }



    private String resolveToken(HttpServletRequest request){ // 토큰정보 획득, 쿠키값 이슈로 'Bearer ' 변경
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer_")){
            return bearerToken.substring(7);
        }
        return null;
    }


    private String resolveCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
                    log.info("쿠키 값 찾기 ={}",cookie.getValue().substring(7));
                    return cookie.getValue().substring(7);
                }
            }
        }

        log.info("쿠키 못꺼내옴");
        return null;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {

        // 1. Request Header 에서 토큰을 꺼냄
//        String jwt = resolveToken(servletRequest);

        // 1.  쿠키에서 값 꺼내야 됨
        String jwt = resolveCookie(servletRequest);
        log.info("jwt 값={}",jwt);

        String requestURI = servletRequest.getRequestURI();

        // 2. validateToken 으로 토큰 유효성 검사
        // 정상 토큰이면 해당 토큰으로 Authentication 을 가져와서 SecurityContext 에 저장

        if(StringUtils.hasText(jwt)&& tokenProvider.validateToken(jwt)){
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            // 유저 저장.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Security Context, Member_ID ='{}' 인증정보 저장 및 조회 , uri= {} ",authentication.getName(),requestURI);
        }else {
            log.debug("통과 url : {} ",requestURI);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
