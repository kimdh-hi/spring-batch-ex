
create table person (
    id bigint primary key auto_increment,
    name varchar(255),
    age integer
);

insert into person(`name`, `age`) values('kim', 20);
insert into person(`name`, `age`) values('lee', 25);
insert into person(`name`, `age`) values('park', 25);