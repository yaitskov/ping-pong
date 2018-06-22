--liquibase formatted sql
--changeset diaitskov:r1.0.012

ALTER TABLE tournament change rules rules varchar(900) not NULL;

ALTER TABLE bid add column bid smallint unsigned NULL ;
update bid set bid = uid;
ALTER TABLE bid change bid bid smallint unsigned NOT NULL;


ALTER TABLE matches change uid_less bid_less smallint unsigned NULL;
ALTER TABLE matches change uid_more bid_more smallint unsigned NULL;
ALTER TABLE matches change uid_win bid_win smallint unsigned NULL;

ALTER TABLE set_score change uid bid smallint unsigned NOT NULL;

ALTER TABLE match_dispute change plaintiff plaintiff smallint unsigned NOT NULL;

--rollback
