
corona_facts_table_create = ("create table if not exists coronakb.corona_facts\
(\
    country_name    text,\
    country_code    text not null\
        constraint corona_facts_pk\
            primary key,\
    total_confirmed bigint,\
    total_deaths    bigint,\
    total_recovered bigint\
);")

country_dim_table_create = ("create table if not exists coronakb.country_dim\
(\
    country_name    text,\
    total_confirmed bigint,\
    total_deaths    bigint,\
    total_recovered bigint,\
        country_code    text not null \
        constraint country_dim_pk\
            primary key\
);")

news_events_fct_table_create = ("create table if not exists coronakb.news_events_fct\
(\
    country_code text,\
    url          text,\
    date_added   bigint,\
    id           text not null\
        constraint news_events_fct_pk\
            primary key\
);")


create_table_queries = [corona_facts_table_create, country_dim_table_create, news_events_fct_table_create]
