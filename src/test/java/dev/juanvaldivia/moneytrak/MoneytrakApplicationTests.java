package dev.juanvaldivia.moneytrak;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@WithMockUser(roles = "ADMIN")
class MoneytrakApplicationTests {

	@Test
	void contextLoads() {
	}

}
