--liquibase formatted sql
--changeset diaitskov:r1.0.009

alter table matches add column tag char(3);

--rollback

