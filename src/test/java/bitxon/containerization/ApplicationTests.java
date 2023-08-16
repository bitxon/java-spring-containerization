package bitxon.containerization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalManagementPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@LocalManagementPort
	Integer managementPort;

    @Test
    void controllerHello() {
		var result = restTemplate.getForObject("/hello", String.class);
		assertThat(result).isEqualTo("{\"message\":\"Hello\"}");
    }

	@Test
	void actuatorInfo() {
		var result = restTemplate.getForObject("http://localhost:" + managementPort + "/actuator/info", String.class);
		assertThat(result).contains("customGitInfo");
	}

}
