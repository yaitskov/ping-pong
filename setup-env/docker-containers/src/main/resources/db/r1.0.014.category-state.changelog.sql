--liquibase formatted sql
--changeset diaitskov:r1.0.014

alter table category add column state char(3) null;
update category set state = 'End';
alter table category change column state state char(3) not null;

--rollback