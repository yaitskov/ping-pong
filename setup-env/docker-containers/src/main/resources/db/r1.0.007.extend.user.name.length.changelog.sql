--liquibase formatted sql
--changeset diaitskov:r1.0.007

alter table users change column name name varchar(80) not null;
alter table place change column name name varchar(80) not null unique;

--rollback

