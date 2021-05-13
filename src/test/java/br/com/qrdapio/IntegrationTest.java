package br.com.qrdapio;

import br.com.qrdapio.QrdapioApp;
import br.com.qrdapio.ReactiveSqlTestContainerExtension;
import br.com.qrdapio.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { QrdapioApp.class, TestSecurityConfiguration.class })
@ExtendWith(ReactiveSqlTestContainerExtension.class)
public @interface IntegrationTest {
}
