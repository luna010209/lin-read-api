package io.lin.read;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:lin_read_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"gemini.api-key=test-gemini-api-key",
		"gemini.models.image=gemini-2.5-flash-image",
		"gemini.models.text=gemini-3-flash-preview",
		"gemini.models.audio=gemini-2.5-flash-preview-tts"
})
class LinReadApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
