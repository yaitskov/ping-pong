--liquibase formatted sql
--changeset diaitskov:r1.0.006

create table match_dispute(
    did int(11) not null auto_increment primary key,
    tid int(11) not null references tournament(tid),
    mid int(11) not null references matches(mid),
    plaintiff int(11) not null references users(uid),
    judge int(11) null references users(uid),
    status varchar(8) not null,
    created timestamp(3) default current_timestamp(3),
    proposed_score varchar(200) not null,
    judge_comment varchar(40),
    plaintiff_comment varchar(40),
    resolved_at timestamp(3) null);

--rollback

