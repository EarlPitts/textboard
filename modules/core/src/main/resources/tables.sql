CREATE TABLE thread (
    id INTEGER PRIMARY KEY NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    time BIGINT DEFAULT (strftime('%s', 'now')) NOT NULL
);

CREATE TABLE post (
    id INTEGER PRIMARY KEY NOT NULL,
    content TEXT NOT NULL,
    time BIGINT DEFAULT (strftime('%s', 'now')) NOT NULL,
    threadid INTEGER NOT NULL,
    FOREIGN KEY(threadid) REFERENCES thread(id)
);
