package info.jab.ms.rd;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import info.jab.ms.support.TestApplication;

@SpringBootTest(classes = TestApplication.class)
public class BeansAndDependencyTests {
 
	@Autowired
	private ApplicationContext context;

    @Test
    void test() {
        String[] beanNames = context.getBeanDefinitionNames();

        AtomicInteger counter = new AtomicInteger(0);
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            Package beanPackage = beanClass.getPackage();

            if (beanPackage != null) {
                String beanPackageInfo = beanPackage.getName();

                    System.out.println(counter.incrementAndGet());
                //if (beanPackageInfo.contains("org.example.dependency")) {
                    System.out.println("Bean: " + beanName);
                    System.out.println("Package: " + beanPackageInfo);
                    System.out.println();
                //}
            }
        }
    }
}
