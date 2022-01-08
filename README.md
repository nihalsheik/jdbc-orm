
Jdbc jdbc = new Jdbc(ds);

List<Person> s = jdbc.queryForBeanList("select * from tbl_person", Person.class);


s.forEach(per -> {
	System.out.println(per.getId() + "-" + per.getName() + "-" + per.getAge() + "-" + per.getCreateTime());
});