create table mindolph_doc
(
    id         varchar(32)
        constraint mindolph_doc_pk
            primary key,
    file_name  varchar(256)  not null,
    file_path  varchar(1024) not null,
    dataset_id varchar(32)   not null,
    block_count integer,
    embedded bool default FALSE,
    comment  varchar(1024)
);

