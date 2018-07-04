--liquibase formatted sql
--changeset diaitskov:r1.0.013

create table suggest_name(
    requester_uid int(11) not null,
    type char(2) not null,
    pattern char(6) not null unique,
    uid int(11) not null,
    created timestamp(3) default current_timestamp(3),
    foreign key (uid) references users(uid),
    primary key(requester_uid, type, pattern, uid));

create index suggest_name_created_idx on suggest_name(created);

--rollback