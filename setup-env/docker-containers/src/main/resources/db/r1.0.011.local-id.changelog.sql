--liquibase formatted sql
--changeset diaitskov:r1.0.011
-- {"g": 123,"c":12,"m":1231222}
alter table tournament add column counters varchar(30) not null default '{}';


alter table bid drop foreign key bid_cid_idx;
ALTER TABLE category CHANGE cid cid INT(11) NOT NULL;
ALTER TABLE category DROP PRIMARY KEY;
ALTER TABLE category ADD PRIMARY KEY(tid, cid);

alter table bid drop foreign key `bid_group_id`;
ALTER TABLE groups CHANGE gid gid INT(11) NOT NULL;
ALTER TABLE groups DROP PRIMARY KEY;
ALTER TABLE groups ADD PRIMARY KEY(tid, gid);

alter table bid add constraint bid_tid_cid_idx
                   foreign key (tid, cid) references category(tid, cid);
alter table bid add constraint bid_tid_gid_idx
                   foreign key (tid, gid) references groups(tid, gid);

ALTER TABLE matches CHANGE mid mid INT(11) NOT NULL;
ALTER TABLE matches DROP PRIMARY KEY;
ALTER TABLE matches ADD PRIMARY KEY(tid, mid);

ALTER TABLE set_score add column tid INT(11) NULL;
update set_score ss inner join matches m on m.mid = ss.mid
                    set ss.tid = m.tid;
delete from set_score where tid is null;

ALTER TABLE set_score change tid tid INT(11) not NULL;
alter table set_score add constraint set_score_tid_mid_idx
                      foreign key (tid, mid) references matches(tid, mid);


--rollback
