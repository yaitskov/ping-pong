--liquibase formatted sql
--changeset diaitskov:r1.0.005

create table sys_admin(
    said int(11) not null auto_increment primary key,
    login varchar(40) not null unique,
    password varchar(40) not null,
    salt varchar(40) not null,
    created timestamp(3) default current_timestamp(3));

create table users(
    uid int(11) not null auto_increment primary key,
    name varchar(40) not null,
    type varchar(5) not null,
    phone varchar(20),
    want_admin timestamp(3),
    email varchar(60),
    created timestamp(3) default current_timestamp(3),
    banned timestamp(3),
    index `email_idx` (email));

create table country (
    country_id int(11) not null auto_increment primary key,
    name varchar(80) not null unique,
    created timestamp(3) default current_timestamp(3),
    author_id int(11) not null,
    foreign key (author_id) references users(uid));

create table city (
    city_id int(11) not null auto_increment primary key,
    country_id int(11) not null,
    name varchar(90) not null unique,
    created timestamp(3) default current_timestamp(3),
    author_id int(11) not null,
    foreign key (country_id) references country(country_id),
    foreign key (author_id) references users(uid));

create table admin(
    uid int(11) not null primary key,
    said int(11) not null,
    created timestamp(3) default current_timestamp(3),
    foreign key (said) references sys_admin(said),
    foreign key (uid)  references users(uid));

create table sessions(
    token varchar(40) not null primary key,
    uid int(11) not null,
    created timestamp(3) default current_timestamp(3),
    device_info varchar(200),
    foreign key (uid) references users(uid));

create table session_key(
    token varchar(40) not null primary key,
    uid int(11) not null,
    created timestamp(3) default current_timestamp(3),
    foreign key (uid) references users(uid));

create table place(
    pid int(11) not null auto_increment primary key,
    name varchar(40) not null unique,
    created timestamp(3) default current_timestamp(3),
    gps varchar(40) null,
    city_id int(11) not null,
    post_address varchar(200) not null,
    phone varchar(40),
    email varchar(40),
    foreign key (city_id) references city(city_id));

create table place_admin(
    uid int(11) not null references admin(uid),
    pid int(11) not null references place(pid),
    created timestamp(3) default current_timestamp(3),
    type varchar(8) not null, -- author, owner, editor
    primary key (pid, uid));

create table tournament(
    tid int(11) not null auto_increment primary key,
    name varchar(120) not null,
    opens_at timestamp(3),
    previous_tid int(11) null,
    ticket_price float null,
    state varchar(10) not null, -- hidden, announce, draft, open, close, canceled
    created timestamp(3) default current_timestamp(3),
    complete_at timestamp(3) null,
    quits_from_group int(11) not null,
    third_place_match int(11) not null,
    match_score int(11) not null,
    max_group_size int(11) not null,
    pid int(11) not null references place(pid));

alter table tournament
 add constraint tournament_previous_tid
    foreign key (previous_tid)
     references tournament(tid);

create table tournament_admin(
    uid int(11) not null references admin(uid),
    tid int(11) not null references tournament(tid),
    created timestamp(3) default current_timestamp(3),
    type varchar(8) not null, -- author, owner, editor
    primary key (uid, tid));

create table category(
    cid int(11) not null auto_increment primary key,
    tid int(11) not null references tournament(tid),
    name varchar(100) not null);

create table bid(
    uid int(11) not null,
    tid int(11) not null,
    cid int(11) not null,
    gid int(11) null,
    created timestamp(3) default current_timestamp(3),
    updated timestamp(3) null,
    state char(4) not null,
    primary key (uid, tid));

alter table bid add constraint bid_uid_idx
                   foreign key (uid) references users(uid);
alter table bid add constraint bid_tid_idx
                   foreign key (tid) references tournament(tid);
alter table bid add constraint bid_cid_idx
                   foreign key (cid) references category(cid);

create table groups(
    gid int(11) not null auto_increment primary key,
    tid int(11) not null references tournament(tid),
    cid int(11) not null references category(cid),
    state varchar(8) not null, -- open, closed
    sort int(11) not null, -- ordinal number in the tournament category
    label varchar(8) not null,
    quits int(11) not null); -- how much people quits of the group

create table matches(
    mid int(11) not null auto_increment primary key,
    gid int(11) null references groups(gid),
    tid int(11) not null references tournament(tid),
    cid int(11) not null references category(cid),
    type char(4) not null,
    started timestamp(3) null,
    ended timestamp(3) null,
    win_mid int(11) null references matches(mid),
    lose_mid int(11) null references matches(mid),
    state varchar(8) not null, -- draft, place, game, end
    `level` int(11) null, -- 0 final or 3rd place, 1 => 1/2 final, 2 => 1/4
    priority int(11) not null);

create table tables(
    table_id int(11) not null auto_increment primary key,
    label varchar(20) not null,
    pid int(11) not null references place(pid),
    created timestamp(3) default current_timestamp(3),
    mid int(11) null references matches(mid),
    state varchar(8) not null, -- free, busy, archived
    category_id int(11) null references category(cat_id));

create table match_score(
    mid int(11) not null references matches(mid),
    uid int(11) not null references users(uid),
    tid int(11) not null references tournament(tid),
    cid int(11) not null references category(cid),
    won int(1) not null default 0,
    sets_won int(11) not null default 0,
    created timestamp(3) default current_timestamp(3),
    updated timestamp(3) null,
    primary key (mid, uid));

alter table bid
      add constraint bid_group_id
         foreign key (gid)
          references groups(gid);

--rollback
