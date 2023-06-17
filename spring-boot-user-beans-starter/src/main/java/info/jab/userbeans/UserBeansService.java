package info.jab.userbeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint.BeanDescriptor;
import org.springframework.boot.actuate.beans.BeansEndpoint.ContextBeansDescriptor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Service
public class UserBeansService {

    Logger logger = LoggerFactory.getLogger(UserBeansService.class);

	@Autowired
	private BeansEndpoint beansEndpoint;

	@Autowired
	private ApplicationContext context;

	public record BeanDetail(String beanName, String beanPackage) {}

    List<String> getBeansFromBeansEndpoint() {
        List<String> beanList = new ArrayList<>();
        Map<String, ContextBeansDescriptor> context = beansEndpoint.beans().getContexts();
		context.forEach((key, value) -> {
			Map<String, BeanDescriptor> beans = value.getBeans();
			beans.forEach((key2, value2) -> {
				String beanName = value2.getType().getSimpleName();
                beanList.add(beanName);
			});
		});

        return beanList.stream().sorted().toList();
    }

    List<String> getBeansFromApplicationContext() {

        String[] beanNames = context.getBeanDefinitionNames();

        return List.of(beanNames).stream().sorted().toList();
    }

    List<BeanDetail> getBeansDetails() {

        Function<String, String> removePackage = (beanName) -> {
            var beanNameParts = beanName.split("\\.");
            return (beanNameParts.length > 0) ? beanNameParts[beanNameParts.length - 1] : beanName;
        };

        var beansFromBeansEndpoint = getBeansFromBeansEndpoint().stream()
            .map(String::toLowerCase)
            .toList();
        var beanListFromApplicationContext = getBeansFromApplicationContext();

        AtomicInteger notFoundBeans = new AtomicInteger(0);
        List<BeanDetail> result = new ArrayList<>();
        for(String beanName : beanListFromApplicationContext) {
            try {
                Object bean = context.getBean(beanName);
                Class<?> beanClass = bean.getClass();
			    Package beanPackage = beanClass.getPackage();

                //Only add beans from BeansEndpoint
                var beanNameFinal = removePackage.apply(beanName);
                var beanToCheck = beanNameFinal.toLowerCase();

                if (beansFromBeansEndpoint.contains(beanToCheck)) {
			        result.add(new BeanDetail(beanNameFinal, beanPackage.getName()));
                } else {
                    notFoundBeans.incrementAndGet();
                    logger.warn("This bean was not found: {}", beanName);
                }
            } catch (NoSuchBeanDefinitionException e) {
                notFoundBeans.incrementAndGet();
                logger.warn(e.getMessage());
            }
        }
        logger.warn("Total Beans not found: {}", notFoundBeans);

        return result;
    }
}
