package com.nihalsoft.java.jdbc.orm;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class Test {

    public static void main(String[] args) throws Exception {

        DataSource ds = new DriverManagerDataSource("jdbc:mysql://localhost:3306/repos", "root", "Welcome@1");

        Jdbc jdbc = new Jdbc(ds);

        List<Person> s = jdbc.queryForBeanList("select * from tbl_person", Person.class);
        

        s.forEach(per -> {
            System.out.println(per.getId() + "-" + per.getName() + "-" + per.getAge() + "-" + per.getCreateTime());
        });
    }

}
