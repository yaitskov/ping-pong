--liquibase formatted sql
--changeset diaitskov:r1.0.011


alter table bid drop foreign key bid_cid_idx;
ALTER TABLE category CHANGE cid cid tinyint unsigned NOT NULL;
ALTER TABLE category DROP PRIMARY KEY;
ALTER TABLE category ADD PRIMARY KEY(tid, cid);

alter table bid drop foreign key `bid_group_id`;
ALTER TABLE bid change cid cid tinyint unsigned NOT NULL;
ALTER TABLE bid change gid gid tinyint unsigned NULL;

ALTER TABLE `groups` CHANGE gid gid tinyint unsigned NOT NULL;
ALTER TABLE `groups` CHANGE cid cid tinyint unsigned NOT NULL;
ALTER TABLE `groups` DROP PRIMARY KEY;
ALTER TABLE `groups` ADD PRIMARY KEY(tid, gid);

ALTER TABLE match_dispute CHANGE did did tinyint unsigned NOT NULL;
ALTER TABLE match_dispute CHANGE mid mid smallint unsigned NOT NULL;
ALTER TABLE match_dispute DROP PRIMARY KEY;
ALTER TABLE match_dispute ADD PRIMARY KEY(tid, did);


alter table bid add constraint bid_tid_cid_idx
                   foreign key (tid, cid) references category(tid, cid);
alter table bid add constraint bid_tid_gid_idx
                   foreign key (tid, gid) references `groups`(tid, gid);

ALTER TABLE tables CHANGE mid mid smallint unsigned NULL;

ALTER TABLE matches CHANGE mid mid smallint unsigned NOT NULL;
ALTER TABLE matches CHANGE win_mid win_mid smallint unsigned NULL;
ALTER TABLE matches CHANGE lose_mid lose_mid smallint unsigned NULL;

ALTER TABLE matches CHANGE cid cid tinyint unsigned not NULL;
ALTER TABLE matches CHANGE gid gid tinyint unsigned NULL;

ALTER TABLE matches DROP PRIMARY KEY;
ALTER TABLE matches ADD PRIMARY KEY(tid, mid);

ALTER TABLE set_score add column tid INT(11) NULL;
update set_score ss inner join matches m on m.mid = ss.mid
                    set ss.tid = m.tid;
delete from set_score where tid is null;

ALTER TABLE set_score CHANGE mid mid smallint unsigned NOT NULL;
ALTER TABLE set_score change tid tid INT(11) not NULL;
alter table set_score add constraint set_score_tid_mid_idx
                      foreign key (tid, mid) references matches(tid, mid);


--rollback
