package son.roomshare.service;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import son.roomshare.config.security.jwt.TokenDto;
import son.roomshare.config.security.jwt.TokenProvider;
import son.roomshare.config.security.jwt.TokenRequestDto;
import son.roomshare.domain.member.Member;
import son.roomshare.domain.member.dto.LoginMemberRequestDto;
import son.roomshare.domain.member.dto.MemberResponseDto;
import son.roomshare.domain.member.dto.SignUpMemberRequestDto;
import son.roomshare.domain.refreshToken.RefreshToken;
import son.roomshare.repository.MemberRepository;
import son.roomshare.repository.RefreshTokenRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;



    @Transactional(readOnly = true)
    public MemberResponseDto findMemberInfoById(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new RuntimeException("로그인 유저 정보가 없습니다."));
    }

    @Transactional(readOnly = true)
    public MemberResponseDto findMemberInfoByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));
    }


    @Transactional
    public MemberResponseDto signup(SignUpMemberRequestDto memberRequestDto) {
        checkDuplicateEmail(memberRequestDto);
        checkDuplicateNickname(memberRequestDto);

        Member member = memberRequestDto.toMember(passwordEncoder,memberRequestDto.getMemberRole());
        return MemberResponseDto.of(memberRepository.save(member));
    }



    @Transactional
    public TokenDto login(LoginMemberRequestDto memberRequestDto, HttpServletResponse response) {

        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = memberRequestDto.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 4. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);
       // ++ 헤더에 토큰 값 추가! And 쿠키 값으로 토큰값 전달
        response.setHeader("Authorization","Bearer_"+ tokenDto.getAccessToken());
        response.setHeader("Refresh_Token",tokenDto.getRefreshToken());

        // 5. 토큰 발급
        return tokenDto;
    }

    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치하는지 검사
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        // 토큰 발급
        return tokenDto;
    }



    private void checkDuplicateNickname(SignUpMemberRequestDto memberRequestDto) {
        if(memberRepository.existsByNickName(memberRequestDto.getNickName())){
            throw new DuplicateRequestException("중복된 닉네임 입니다");
        }
    }

    private void checkDuplicateEmail(SignUpMemberRequestDto memberRequestDto) {
        if (memberRepository.existsByEmail(memberRequestDto.getEmail())) {
            throw new DuplicateRequestException("중복된 아이디 입니다");
        }
    }

}
