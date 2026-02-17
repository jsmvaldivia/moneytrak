package dev.juanvaldivia.moneytrak;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class MoneytrakApplicationTests {

	@Test
	void contextLoads() {
	}

}
