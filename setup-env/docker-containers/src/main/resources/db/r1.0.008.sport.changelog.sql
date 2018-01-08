--liquibase formatted sql
--changeset diaitskov:r1.0.008

alter table tournament add column sport varchar(8) not null default 'PingPong';

--rollback

