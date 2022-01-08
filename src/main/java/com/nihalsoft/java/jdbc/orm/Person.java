package com.nihalsoft.java.jdbc.orm;

import java.time.LocalDateTime;

import com.nihalsoft.java.jdbc.orm.annotation.Column;
import com.nihalsoft.java.jdbc.orm.annotation.Id;
import com.nihalsoft.java.jdbc.orm.annotation.Table;

@Table(name = "tbl_person")
public class Person {

    private Long id;
    private String name;
    private Integer age;
    private LocalDateTime createTime;

    @Id
    @Column
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Column(name = "create_time")
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

}
