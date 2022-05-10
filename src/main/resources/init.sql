DROP TABLE if EXISTS users;
DROP TABLE if EXISTS files;

CREATE TABLE users
(
    id       INTEGER     NOT NULL PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(34) NOT NULL,
    role     INTEGER(4) NOT NULL,
    created  INTEGER(11) NOT NULL
);

CREATE TABLE files
(
    id       INTEGER      NOT NULL PRIMARY KEY,
    path     VARCHAR(512) NOT NULL,
    author   INTEGER      NOT NULL,
    download INTEGER      NOT NULL,
    remark   VARCHAR(256) NOT NULL,
    created  bigint       NOT NULL
);