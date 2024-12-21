package hello.login.domain.login;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    /**
     *
     * @return  null 로그인 실패
     */
    public Member login(String loginId, String password) {
        /*
        Optional<Member> findMemberOptional = memberRepository.findByLoginId(loginId);
        Member member = findMemberOptional.get();
        if(member.getPassword().equals(password)) {
            return member;
        }else {
            return null;
        }
        */

        Optional<Member> findMemberOptional = memberRepository.findByLoginId(loginId);
        return findMemberOptional.filter(member -> member.getPassword().equals(password))
            .orElse(null);

    }
}
