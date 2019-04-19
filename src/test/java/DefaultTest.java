import com.hand.oauth.OauthApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OauthApplication.class)
public class DefaultTest {
    @Test
    public void passwordEncoderTest() {
        PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        //默认使用bcrypt进行加密，且每次加密的结果都不一样
        System.out.println("bCrypt:++"+delegatingPasswordEncoder.encode("123456"));
        System.out.println("matches:+++++++"+delegatingPasswordEncoder.matches("123456", "{bcrypt}$2a$10$mHv8keEqSLlYY27H.Wu/6.ACxX6Whe5mFXrP6j75Tw0dwQD5MclC."));
    }

    @Qualifier("userDetailsServiceImpl")
    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    public void userDetailsMapperTest() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("donglin.ling@hand-china.com");
        userDetailsService.loadUserByUsername("15797656200");
    }
}
