package com.spring.boot;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootBoardJspApplication {

	@Autowired
	ApplicationContext applicationContext;
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootBoardJspApplication.class, args);
	}
	
	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception{
		
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		
		/*
		Resource[] res = new PathMatchingResourcePatternResolver()
				.getResources("classpath:mybatis/mapper/*.xml");
		sessionFactory.setMapperLocations(res);
		*/
		
		sessionFactory.setDataSource(dataSource);
		sessionFactory.setMapperLocations(
				applicationContext.getResources("classpath:mybatis/mapper/*.xml"));
		
		return sessionFactory.getObject();
	}

}
