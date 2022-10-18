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

    /**
     * 파라미터 사용하는 방법
     */
    @Test
    void argumentMessage() {
        // 매개변수는 무조건 Object 배열로 넘겨야 한다.
        String message = ms.getMessage("hello.name", new Object[]{"Spring!"}, null);
        assertThat(message).isEqualTo("안녕 Spring!");
    }

    /**
     * 이번엔 국제화 테스트를 해보자
     * 일반 디폴트
     */
    @Test
    void defaultLang() {
        assertThat(ms.getMessage("hello", null, null)).isEqualTo("안녕");
        assertThat(ms.getMessage("hello", null, Locale.KOREA)).isEqualTo("안녕");
    }

    /**
     * 해외로 가볼까여?
     */
    @Test
    void enLang() {
        assertThat(ms.getMessage("hello", null, Locale.ENGLISH)).isEqualTo("hello");
    }
}
