import com.company.bbkb.service.IDemoService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author: yangyl
 * @Date: 2020-05-02 15:11
 * @Description:
 */
public class DemoTest {
	@Test
	public void test(){
		ApplicationContext context= new ClassPathXmlApplicationContext("classpath:spring.xml");
		IDemoService demoService = context.getBean("demoService", IDemoService.class);
		demoService.sayHello();
	}
}
