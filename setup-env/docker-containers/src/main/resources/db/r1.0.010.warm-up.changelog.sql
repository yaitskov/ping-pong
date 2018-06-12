--liquibase formatted sql
--changeset diaitskov:r1.0.010

create table warm_up (
    wm_id int(11) not null auto_increment primary key,
    before_action varchar(100) not null,
    uid int(11) not null,
    created timestamp(3) default current_timestamp(3),
    warm_up_started timestamp(3) not null,
    client_started timestamp(3) null,
    complete_at timestamp(3) null,
    foreign key (uid) references users(uid));

--rollback

