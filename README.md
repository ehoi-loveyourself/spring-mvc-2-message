## 메시지, 국제화란?

### 메시지

다양한 메시지를 한 곳에서 관리할 수 있는 기능을 메시지 기능이라고 한다.

예를 들어 `messages.properties` 라는 메시지 관리용 파일을 만들고 아래와 같이 관리한다면

```
item=상품
item.id=상품 ID
item.itemName=상품명
item.price=가격
item.quantity=수량
```

`상품명`이라는 단어를 `상품이름` 이라고 수정해야 할때 properties 파일만 수정하면 되기 때문에 매우 편리하다.

그리고 각 html에서는 다음과 같이 불러와서 사용하면 되겠다.

```html
<label for="itemName" th:text="#{item.itemName}"></label>
```

### 국제화

메시지에서 레벨을 높여보자

이제 이 메시지를 나라별로 별도로 관리하면 서비스를 국제화할 수 있다.

`messages_ko.properties`

```
item=상품
item.id=상품 ID
item.itemName=상품명
item.price=가격
item.quantity=수량
```

`messages_en.properties`

```
item=item
item.id=item ID
item.itemName=item name
item.price=price
item.quantity=quantity
```

한국에서 접근한 것인지 영어권에서 접근한 것인지 인식하는 방법은 HTTP의 `accept-language` 헤더 값을 사용하거나, 사용자가 직접 언어를 선택하도록 하고 쿠키에 저장해서 처리하면 된다.

### 스프링에서 이걸 제공한다고?

직접 구현할 수도 있겠지만, 스프링에서 메시지와 국제화 기능을 모두 제공한다고 한다. 그리고 타임리프 또한 스프링이 제공하는 기능을 편리하게 통합해서 제공한다.

## 스프링 메시지 소스 설정

### 직접 등록

메시지 기능을 사용하기 위해서 첫번째 방법은 `MessageSource`를 `직접` 빈으로 등록하는 것이다.

`MessageSource`는 인터페이스이므로 구현체인 `ResourceBundleMessageSource`를 등록하면 된다.

```java
@Bean
  public MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasenames("messages", "errors");
      messageSource.setDefaultEncoding("utf-8");
      return messageSource;
}
```

- `basenames` : 설정 파일의 이름을 지정한다.
    - `messages` 로 지정하면 `messages.properties` 파일을 읽어서 사용한다.
    - 만약 국제화 기능을 이용하려면 `messages_en.properties`, `messages_ko.properties` 와 같이 파일명 마지막에 언어 정보를 주면 된다. 만약 찾을 파일이 없다면 디폴트로 `messages.properties` 을 사용한다.
    - 파일의 위치는 `/resource/messages.properties` 에 두면 된다.
    - 여러 파일을 한 번에 지정할 수 있다. 위의 코드에서는 messages, errors 두가지만 지정했다.
- defaultEncoding : 인코딩 정보를 저장한다. utf-8을 사용하면 된다.

### 스프링 부트

스프링 부트를 사용해서 MessageSource를 자동으로 빈으로 등록한다.

******************소스 설정******************

`application.properties`

```java
spring.messages.basename=messages,config.i18n.messages
```

******스프링 부트 메시지 소스 기본 값******

spring.messages.basename=messages

************************************************메시지 파일 만들기************************************************

- `messages.properties` : 기본값으로 사용할 파일
- `messages_en.properties` : 영어 국제화로 사용할 파일

`messages.properties`

```
hello=안녕 
hello.name=안녕 {0}
```

`messages_en.properties`

```
hello=hello
hello.name=hello {0}
```

이제 테스트 코드를 통해서 학습해보자!

```java
package hello.itemservice.message;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class MessageSourceTest {

    @Autowired
    MessageSource ms;

    @Test
    void messageHello() {
        String result = ms.getMessage("hello", null, null);
        assertThat(result).isEqualTo("안녕");
    }
}
```

- ms.getMessage("hello", null, null)
    - code : hello
    - args : null
    - locale : null

가장 간단한 테스트로 메시지 코드는 hello, 나머지 값은 null을 입력했다

locale 정보가 없으면 basename에서 설정한 기본 이름 메시지 파일을 조회한다.

basename으로 messages를 지정했기 때문에 messages.properties 파일에서 데이터를 조회한다.

******메시지가 없는 경우, 기본 메시지 Test******

```java
@Test
void notFoundMessageCode() {
    assertThatThrownBy(() -> ms.getMessage("no code", null, null))
            .isInstanceOf(NoSuchMessageException.class);
    // no code 라는 코드는 없기 때문에 NoSuchMessageException이 터져서 테스트가 성공하는 것을 볼 수 있다
    // 아래의 코드를 실행한다면 에러가 터지는 것을 볼 수 있을 것!
    // ms.getMessage("no code", null, null)
}

/**
 * 코드를 못 찾았을 때 띄울 기본 메시지 설정
 */
@Test
void notFoundMessageCodeDefaultMessage() {
    String result = ms.getMessage("no code", null, "기본 메시지", null);
    assertThat(result).isEqualTo("기본 메시지");
}
```

- 메시지가 없을 경우에는 NoSuchMessageException이 발생한다.
- 메시지가 없어도 기본 메시지 파라미터(defaultMessage)를 사용하면 기본 메시지가 반환된다.

******매개변수 사용 Test******

```java
/**
 * 파라미터 사용하는 방법
 */
@Test
void argumentMessage() {
    // 매개변수는 무조건 Object 배열로 넘겨야 한다.
    String message = ms.getMessage("hello.name", new Object[]{"Spring!"}, null);
    assertThat(message).isEqualTo("안녕 Spring!");
}
```

- messages.properties의 `hello.name=안녕 {0}` 에서 {0} 부분을 `Spring!`이 대체하여 `안녕 Spring!` 이 된다.

************************************국제화 파일을 사용해보자************************************

locale 정보를 기반으로 스프링 부트가 국제화 파일을 선택하는데

만약 Locale이 `en_US` 로 맞춰져 있다면 `messages_en_US` → `messages_en` → `messages` 순으로 해당 파일이 있는지 찾고 있으면 사용한다.

없으면 default은 messages를 사용하는 셈!

********Test********

```java
/**
 * 이번엔 국제화 테스트를 해보자
 * 일반 디폴트
 */
@Test
void defaultLang() {
    assertThat(ms.getMessage("hello", null, null)).isEqualTo("안녕"); // (1)
    assertThat(ms.getMessage("hello", null, Locale.KOREA)).isEqualTo("안녕"); // (2)
}

/**
 * 해외로 가볼까여?
 */
@Test
void enLang() {
    assertThat(ms.getMessage("hello", null, Locale.ENGLISH)).isEqualTo("hello"); // (3)
}
```

- (1) 에서는 locale 정보가 없어서 `Locale.getDefault()`를 호출해서 시스템의 기본 로케일을 사용 : 시스템 기본 로케일이 ko_KR 이므로 `messages_ko` 찾음 → 없음 → `messages` 를 사용
- (2) 에서는 KOREA로 지정되어 있지만? `messages_ko` 가 없어서 `messages` 사용
- (3) 에서는 ENGLISH로 되어있으니 `messages_en` 을 찾아서 사용

### 웹 애플리케이션에 메시지 적용하기

************************************************타임리프 메시지 적용************************************************

#{…} 를 사용하면 스프링의 메시지를 편하게 가져올 수 있다.

properties에 작성해두고 #{label.item} 처럼 조회하면 그에 해당하는 값을 불러올 수 있다.

****파라미터는****

`hello.name=안녕 {0}`  이것을 사용한다고 치자! 아래와 같이 사용하면 된다!

```html
<p th:text="#{hello.name(${item.itemName})}"></p>
```

### 웹 애플리케이션에 국제화 적용하기

영어 메시지를 messages_en.properties 에 추가한다!

그럼 끝이다.

우리가 메시지를 타임리프에 미리 적용을 다 해두었기 때문이다.

그래서 웹 브라우저가 현재 클라이언트의 locale을 인식하고 그에 맞는 프로퍼티를 찾을 것이다!

**********************************확인해보려면**********************************

크롬 브라우저에서 설정 → 언어를 검색하고 언어 우선순위를 변경하면 된다!

이렇게 언어를 변경하면 요청시에 `Request-Header`에서 `Accept-Language` 값이 변경된다.

아래의 그림을 보면 en-US가 가장 먼저 위치해있는 것을 볼 수 있다.

`Accept-Language`는 클라이언트가 서버에 기대하는 언어 정보를 담아서 요청하는 것이라고 한다.

![Accept-Language.png](./assets/readme.png)

### 스프링의 국제화 메시지 선택

메시지 기능은 `Locale` 정보를 알아야 언어를 선택할 수 있다.

그래서 스프링도 이를 해석하기 위해서 `Accept-Language` 의 값을 사용한다

조금 더 자세히 살펴보자면

스프링은 `Locale` 선택 방식을 변경할 수 있도록 `LocaleResolver` 라는 인터페이스를 제공하는데, 스프링 부트는 기본으로 `Accept-Language` 를 활용하는 `AcceptHeaderLocaleResolver` 를 사용한다.

그래서 만약 서비스를 개발하다가 사용자가 선택한 언어를 바탕으로 메시지를 보여주고 싶다면, 쿠키나 세션 기반으로 `LocaleResolver`의 구현체를 오버라이딩해서 사용할 수 있을 것이다. 이와 관련해서는 수많은 예제가 나온다고 하니 그것을 참고하자

국제적으로 서비스를 하는 회사가 아니라면, 국제화 기능은 많이 사용할 일이 없을 것이다. 하지만 메시지 기능은 사용하기 유용하므로 꼭 알아두도록 하자!
