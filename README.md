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

