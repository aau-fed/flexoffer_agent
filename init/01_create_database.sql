create database if not exists foa;
create database if not exists fmanproxy;
grant all on foa.* to 'fed';
grant all on fmanproxy.* to 'fed';