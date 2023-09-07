create table users (
	login varchar(50) primary key,
	password varchar (64) not null,
	creation_date timestamp not null,
	last_login timestamp not null
);


