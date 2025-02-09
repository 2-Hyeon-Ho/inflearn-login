# 12.21 공부내용
### 1. 로그인처리  
로그인 시도한 ID로 대상을 조회하고 입력한 패스워드와 비교하여 일치하면 정상로그인  
일치하지 않으면 bindingResult 통해서 글로벌 오류 발생  

### 2. 쿠키
로그인 상태를 유지하기 위해 쿠키를 사용  

- 영속쿠키 : 만료 날짜 입력 시 해당 날짜까지 유지
- 세션쿠키 : 만료 날짜 생략 시 브라우저 종료시까지 유지

쿠키를 생성하여 넘겨줄 때 HttpServletResponse를 통해 전달  
```
@PostMapping("/login")
    public String login(
        @Validated @ModelAttribute("loginForm") LoginForm loginForm,
        BindingResult bindingResult,
        HttpServletResponse response
        ) {
        
        ...

        //쿠키에 시간정보를 주지 않으면 세션쿠키(브라우저 종료시 쿠키 유효가 만료)
        Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
        response.addCookie(idCookie);

        return "redirect:/";
    }
```

쿠키정보를 통해 로그인에 성공한 회원은 로그인 성공 화면으로 이동  
이때, 쿠키정보는 @CookieValue 어노테이션을 통해 쿠키를 조회할 수 있다.

```
@GetMapping("/")
    public String homeLogin(@CookieValue(name = "memberId", required = false) Long memberId, Model model) {

        if(memberId == null) {
            return "home";
        }

        //로그인된 상태
        Member loginMember = memberRepository.findById(memberId);
        if(loginMember == null) {   //쿠키가 오래돼서 없을 수 있음
            return "home";
        }

        model.addAttribute("member", loginMember);
        return "loginHome";
    }
```

위와 같이 쿠키에 memberId를 넘겨주어 로그인을 유지하게 되면 1,2,3 처럼 단순한 값으로
추측하기 쉽고 **클라이언트에서 쿠키의 값을 변경할 수 있으므로 심각한 보안문제가 된다.**  
#### 대안
- 쿠키에 예측가능하거나 중요정보를 세팅하지 않고 임의의 토큰정보(UUID)를 세팅하고 서버에서 토큰을 관리
- 토큰값이 탈취당해도 재사용이 불가하도록 토큰의 만료시간을 짧게 유지한다.(Ex. 30분)


### 3. 로그아웃처리
쿠키의 종료일자를 0으로 세팅해서 넘겨주기
```
    cookie.setMaxAge(0);
    response.addCookie(cookie);
```

# 02.09 공부내용
### 세션
세션을 통해 서버와 클라이언트간 중요정보를 전달하여 관리  

세션을 직접 생성하여 개발  
1. 세션생성
- sessionId 생성(임의의 추정 불가한값으로 생성(UUID))
- 세션저장소(서버)에 sessionId와 보관할 값 저장
- sessionId로 응답쿠키를 클라이언트에 전달
2. 세션조회
- 클라이언트가 요청한 sessionId의 쿠키값으로 세션저장소(서버)에서 보관한 값 조회
3. 세션만료
- 클라이언트가 요청한 sessionId의 쿠키값으로 세션저장소(서버)에서 sessionId와 보관한 값 제거

**테스트시에는 HttpServletResponse, HttpServletRequest 를 직접 사용할 수 없으므로 MockHttpServletRequest, Response를 사용**

### 스프링 세션
스프링에서는 @SessionAttribute를 통해 세션을 편리하게 사용할 수 있도록 지원  
```
@GetMapping("/")
    public String homeLoginV3Spring(
        @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member member,
        Model model) {

        //세션에 회원데이터가 없으면 home
        if(member == null) {
            return "home";
        }

        //세션이 유지되면 로그인으로 이동
        model.addAttribute("member", member);
        return "loginHome";
    }
```

### 세션의 타임아웃
세션은 메모리에 생성하므로 특정시간이 지나면 삭제가 필요하다.  
기본 조건은 30분 유지이고 지나면 삭제된다.
```
session.setMaxInactiveInterval(1800); //1800초
```
보통 30분마다 세션정보를 삭제한다면 세션을 사용중이더라도 30분마다 삭제되어 클라이언트 입장에서 번거러움이 있어 
최근 세션요청시간으로 부터 30분뒤에 세션을 삭제하는 방식 택함