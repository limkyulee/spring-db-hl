package hello.itemservice;

import hello.itemservice.config.*;
import hello.itemservice.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;


//@Import(MemoryConfig.class)
//@Import(JdbcTemplateV1Config.class)
//@Import(JdbcTemplateV2Config.class)
@Slf4j
@Import(JdbcTemplateV3Config.class)
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")
public class ItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

	@Bean
	@Profile("local")
	public TestDataInit testDataInit(ItemRepository itemRepository) {
		return new TestDataInit(itemRepository);
	}

	/**
	 * 테스트 케이스인 경우에만 해당 DataSource 를 적용.
	 * 임베디드 모드로 동작(메모리 모드)하는  H2 데이터베이스를 사용할 수 있음.
	 * 해당 데이터소스를 사용하면 메모리 DB 를 사용할 수 있음.
	 * @return dataSource
	 *
	 * 사용 시, 실제 DB 는 내려주어야함.
	 * 메모리 DB 에 테이블도 만들어 주어야함. (src/test/resources/schema.sql)

	 * spring 임배디드 db 사용 시, 따로 설정 안해줘도 됨.
	 * 사실 스프링이 따로 서버 설정 안해주면 임베디드 모드로 알아서 설정해줌.
	 */

//	@Bean
//	@Profile("test")
//	public DataSource dataSource() {
//		log.info("메모리 데이터베이스 초기화");
//
//		DriverManagerDataSource dataSource = new DriverManagerDataSource();
//		dataSource.setDriverClassName("org.h2.Driver");
//		dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
//		dataSource.setUsername("sa");
//		dataSource.setPassword("");
//		return dataSource;
//	}

}
