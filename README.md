# roomshare
- 룸쉐어 서비스 (집 공유) 

# 기술 ver
java : 11
spring boot : 2.7.12


# library 
Spring data Jpa
Thymeleaf
Spring Security
Validation


# 1.  인증 방식 고려 
토큰 기반 인증 방식과 세션 기반 인증 방식은 각각 장단점을 가지고 있다.

## 1. 토큰 기반 인증 방식의 장점:

확장성: 토큰은 서버의 인증 상태를 저장하지 않고, 클라이언트 측에 저장하기 때문에 서버의 확장성이 용이합니다. 다중 서버 환경에서도 각 서버가 독립적으로 토큰을 검증할 수 있습니다.
분리된 인증과 인가: 토큰 기반 인증은 인증과 인가를 분리할 수 있습니다. 토큰은 클라이언트가 인증된 사용자임을 증명하고, 서버는 해당 토큰의 권한을 확인하여 인가를 처리합니다.
클라이언트와 서버 간의 상태 비저장: 토큰은 서버에 상태를 저장하지 않고, 클라이언트의 요청에 응답하는 데 필요한 모든 정보를 포함하고 있기 때문에 서버에 부담을 주지 않습니다.

#### 하지만
토큰 중 하나인 JWT는 사용자 인증 정보와 토큰의 발급시각, 만료시각, 토큰의 ID등 담겨있는 정보가 
세션 ID에 비해 비대하므로 세션 방식보다 훨씬 더 많은 네트워크 트래픽을 사용한다.
또한!
토큰은 서버가 트래킹하지 않고, 클라이언트가 모든 인증정보를 가지고 있다. 
따라서 토큰이 한번 해커에게 탈취되면 해당 토큰이 만료되기 전까지는 속수무책으로 피해를 입을 수 밖에 없다.


## 2.세션 기반 인증 방식의 장점:

간편한 구현: 세션 기반 인증은 일반적으로 세션을 사용하여 인증을 처리하기 때문에 개발 및 구현이 비교적 간단합니다.
서버 측에서 세션 관리: 서버가 세션 데이터를 관리하고 유효성을 검사하기 때문에 클라이언트는 세션에 대한 걱정을 할 필요가 없습니다.
세션 데이터의 변경 관리: 세션은 서버 측에서 관리되므로 세션 데이터의 변경이 필요할 경우 서버 측에서 처리할 수 있습니다.

#### 하지만

일반적으로 웹 어플리케이션의 서버 확장 방식은 수평 확장을 사용한다. 
즉, 한대가 아닌 여러대의 서버가 요청을 처리하게 된다. 이때 별도의 작업을 해주지 않는다면, 세션 기반 인증 방식은 세션 불일치 문제를 겪게 된다.
만약 세션 정보를 공유하지 않으면 사용자가 한 서버에서 로그인한 후에 다른 서버로 이동했을 때 로그인 상태가 유지되지 않게된다.
다중 서버 환경에서 세션 기반 인증을 사용할 때는 세션 정보를 공유하기 위해서 세션 공유방법이 필요하다!




---------------------

https://github.com/jobmania/CloneNaverShopping
https://github.com/jobmania/It-commerce_BE-