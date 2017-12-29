--liquibase formatted sql
--changeset diaitskov:r1.0.008

alter table tournament add column type char(7) not null default 'Classic';

create table tournament_relation (
    type varchar(7) not null,  -- console league
    parent_tid int(11) not null,
    child_tid int(11) not null,
    primary key (type, parent_tid, child_tid));

--rollback

